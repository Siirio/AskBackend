package kz.ask.identity.application;

import java.util.Comparator;
import java.util.List;
import kz.ask.business.domain.BranchMemberService;
import kz.ask.business.domain.BusinessService;
import kz.ask.business.domain.dto.BusinessRegistrationResult;
import kz.ask.identity.api.dto.AuthBusinessContextResponse;
import kz.ask.identity.api.dto.AuthSessionResponse;
import kz.ask.identity.api.dto.AuthUserResponse;
import kz.ask.identity.api.dto.ChangeTemporaryPasswordRequest;
import kz.ask.identity.api.dto.LoginRequest;
import kz.ask.identity.api.dto.RoleOption;
import kz.ask.identity.api.dto.SelectRoleRequest;
import kz.ask.identity.domain.IdentityService;
import kz.ask.identity.domain.dto.AppUserDto;
import kz.ask.identity.domain.dto.AuthChallengeDto;
import kz.ask.identity.domain.dto.AuthSessionDto;
import kz.ask.identity.domain.enums.AppRole;
import kz.ask.identity.domain.enums.AuthChallengeChannel;
import kz.ask.identity.domain.enums.AuthChallengePurpose;
import kz.ask.identity.domain.enums.UserStatus;
import kz.ask.identity.infrastructure.mail.EmailCodeSender;
import kz.ask.identity.infrastructure.security.AskPrincipal;
import kz.ask.shared.error.AuthException;
import kz.ask.shared.error.ErrorCode;
import kz.ask.shared.error.ForbiddenException;
import kz.ask.shared.error.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class LoginProcessor {

    private final IdentityService identityService;
    private final BranchMemberService branchMemberService;
    private final BusinessService businessService;
    private final EmailCodeSender emailSender;

    public AuthSessionResponse login(LoginRequest req) {
        List<AppUserDto> users = identityService.findAllByEmail(req.getEmail());
        if (users.isEmpty()) {
            throw new AuthException(ErrorCode.INVALID_CREDENTIALS);
        }

        AppUserDto passwordMatch = users.stream()
                .filter(u -> identityService.verifyPassword(req.getPassword(), u.getPasswordHash()))
                .findFirst()
                .orElse(null);
        if (passwordMatch == null) {
            throw new AuthException(ErrorCode.INVALID_CREDENTIALS);
        }

        List<AppUserDto> activeUsers = users.stream()
                .filter(u -> u.getStatus() == UserStatus.ACTIVE || u.getStatus() == UserStatus.PENDING_ACTIVATION)
                .filter(u -> u.getStatus() != UserStatus.BLOCKED && u.getStatus() != UserStatus.DELETED)
                .toList();

        if (activeUsers.isEmpty()) {
            throw new ForbiddenException(ErrorCode.ACCOUNT_NOT_ACTIVE);
        }

        List<String> allRoles = activeUsers.stream()
                .map(u -> u.getRole().name())
                .distinct()
                .toList();

        if (activeUsers.size() == 1) {
            return loginSingleRole(activeUsers.get(0), allRoles);
        }

        AppUserDto lastUsed = activeUsers.stream()
                .filter(u -> u.getLastLoginAt() != null)
                .max(Comparator.comparing(AppUserDto::getLastLoginAt))
                .orElse(null);
        if (lastUsed != null) {
            return loginSingleRole(lastUsed, allRoles);
        }

        AppUserDto activationUser = activeUsers.stream()
                .filter(AppUserDto::getMustChangePassword)
                .findFirst()
                .orElse(null);
        if (activationUser != null) {
            return loginSingleRole(activationUser, allRoles);
        }

        List<RoleOption> availableRoles = activeUsers.stream()
                .map(u -> RoleOption.builder()
                        .userId(u.getId())
                        .role(u.getRole().name())
                        .displayName(u.getDisplayName())
                        .build())
                .toList();

        return AuthSessionResponse.builder()
                .requiresRoleSelection(true)
                .availableRoles(availableRoles)
                .allRoles(allRoles)
                .build();
    }

    public AuthSessionResponse selectRole(SelectRoleRequest req) {
        AppUserDto user = identityService.findByEmailAndRole(req.getEmail(), AppRole.valueOf(req.getRole()));
        if (user == null) {
            throw new AuthException(ErrorCode.INVALID_CREDENTIALS);
        }
        if (user.getStatus() == UserStatus.BLOCKED || user.getStatus() == UserStatus.DELETED) {
            throw new ForbiddenException(ErrorCode.ACCOUNT_NOT_ACTIVE);
        }
        if (!identityService.verifyPassword(req.getPassword(), user.getPasswordHash())) {
            throw new AuthException(ErrorCode.INVALID_CREDENTIALS);
        }
        List<String> allRoles = identityService.findAllByEmail(user.getEmail()).stream()
                .filter(u -> u.getStatus() == UserStatus.ACTIVE)
                .map(u -> u.getRole().name())
                .distinct()
                .toList();
        return loginSingleRole(user, allRoles);
    }

    private AuthSessionResponse loginSingleRole(AppUserDto user, List<String> allRoles) {
        identityService.recordLogin(user.getId());

        BusinessRegistrationResult bizResult = resolveBusiness(user);

        if (user.getMustChangePassword()) {
            String authority = resolveAuthority(user);
            Long ttl = identityService.staffActivationSessionTtl();
            AuthSessionDto session = identityService.createSession(user.getId(), authority, false, ttl, true);
            return buildSessionResponse(session, user, bizResult, allRoles);
        }

        if (Boolean.TRUE.equals(user.getTwoFactorEnabled())) {
            AuthChallengeDto challenge = identityService.createChallenge(
                    user.getId(), user.getEmail(),
                    AuthChallengeChannel.EMAIL, AuthChallengePurpose.LOGIN,
                    false, null);
            emailSender.sendCode(user.getEmail(), challenge.getCodePlain());
            return AuthSessionResponse.builder()
                    .requiresTwoFactor(true)
                    .authChallengeId(challenge.getId())
                    .user(buildUserResponse(user))
                    .allRoles(allRoles)
                    .build();
        }

        String authority = resolveAuthority(user);
        AuthSessionDto session = identityService.createSession(user.getId(), authority, false);
        return buildSessionResponse(session, user, bizResult, allRoles);
    }

    private BusinessRegistrationResult resolveBusiness(AppUserDto user) {
        if (isBusinessRole(user.getRole())) {
            return businessService.findByOwner(user.getId());
        }
        return null;
    }

    @Transactional
    public AuthSessionResponse changeTemporaryPassword(AskPrincipal principal, ChangeTemporaryPasswordRequest req) {
        if (!req.getNewPassword().equals(req.getPasswordConfirmation())) {
            throw new ValidationException(ErrorCode.PASSWORDS_DO_NOT_MATCH);
        }

        AppUserDto user = identityService.findById(principal.getUserId());
        if (user == null || !user.getMustChangePassword()) {
            throw new ValidationException(ErrorCode.PASSWORD_CHANGE_NOT_REQUIRED);
        }

        identityService.activateStaff(user.getId(), req.getNewPassword());
        identityService.logout(principal.getUserId());

        String authority = resolveAuthority(user);
        AuthSessionDto session = identityService.createSession(user.getId(), authority, false);
        BusinessRegistrationResult bizResult = resolveBusiness(user);
        List<String> allRoles = identityService.findAllByEmail(user.getEmail()).stream()
                .filter(u -> u.getStatus() == UserStatus.ACTIVE)
                .map(u -> u.getRole().name())
                .distinct()
                .toList();
        return buildSessionResponse(session, user, bizResult, allRoles);
    }

    private String resolveAuthority(AppUserDto user) {
        if (user.getRole() == AppRole.CUSTOMER) {
            return "ROLE_CUSTOMER";
        }
        if (branchMemberService.isBranchStaff(user.getId())) {
            return "ROLE_BUSINESS_STAFF";
        }
        return "ROLE_BUSINESS_OWNER";
    }

    private AuthSessionResponse buildSessionResponse(AuthSessionDto session, AppUserDto user,
                                                      BusinessRegistrationResult bizResult, List<String> allRoles) {
        AuthSessionResponse.AuthSessionResponseBuilder builder = AuthSessionResponse.builder()
                .tokenType("Bearer")
                .accessToken(session.getPlainToken())
                .expiresAt(session.getExpiresAt())
                .remembered(session.getRemembered())
                .activationRequired(session.getActivationRequired())
                .role(session.getAuthority())
                .startRoute(resolveStartRoute(session.getAuthority(), user))
                .user(buildUserResponse(user))
                .allRoles(allRoles);

        if (bizResult != null) {
            builder.business(AuthBusinessContextResponse.builder()
                    .businessId(bizResult.getBusiness().getId())
                    .businessName(bizResult.getBusiness().getName())
                    .branchId(bizResult.getBranch().getId())
                    .branchName(bizResult.getBranch().getName())
                    .membershipId(bizResult.getMember().getId())
                    .memberRole(bizResult.getMember().getRole())
                    .build());
        }

        return builder.build();
    }

    private AuthUserResponse buildUserResponse(AppUserDto user) {
        return AuthUserResponse.builder()
                .userId(user.getId())
                .displayName(user.getDisplayName())
                .email(user.getEmail())
                .status(user.getStatus().name())
                .build();
    }

    private String resolveStartRoute(String authority, AppUserDto user) {
        if (user.getRole() == AppRole.CUSTOMER) {
            return "CLIENT_SEARCH";
        }
        if (authority != null && authority.contains("OWNER")) {
            return "OWNER_BRANCHES";
        }
        return "BRANCH_WORKSPACE";
    }

    private boolean isBusinessRole(AppRole role) {
        return role == AppRole.BUSINESS_OWNER
                || role == AppRole.BUSINESS_MANAGER
                || role == AppRole.BUSINESS_WORKER;
    }
}

package kz.ask.identity.application;

import java.util.List;
import java.time.Duration;
import java.time.Instant;
import kz.ask.business.domain.BusinessService;
import kz.ask.business.domain.dto.BusinessRegistrationResult;
import kz.ask.identity.api.dto.AuthBusinessContextResponse;
import kz.ask.identity.api.dto.AuthSessionResponse;
import kz.ask.identity.api.dto.AuthUserResponse;
import kz.ask.identity.api.dto.ChangeTemporaryPasswordRequest;
import kz.ask.identity.api.dto.LoginRequest;
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
import kz.ask.identity.infrastructure.security.JwtTokenService;
import kz.ask.shared.error.AuthException;
import kz.ask.shared.error.ErrorCode;
import kz.ask.shared.error.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class LoginProcessor {

    private final IdentityService identityService;
    private final BusinessService businessService;
    private final EmailCodeSender emailSender;
    private final SessionCapabilitiesProcessor sessionCapabilitiesProcessor;
    private final JwtTokenService jwtTokenService;

    public AuthSessionResponse login(LoginRequest req) {
        List<AppUserDto> users = identityService.findAllByEmail(req.getEmail());
        if (users.isEmpty()) {
            throw new AuthException(ErrorCode.INVALID_CREDENTIALS);
        }

        List<AppUserDto> activeUsers = users.stream()
                .filter(u -> u.getStatus() == UserStatus.ACTIVE || u.getStatus() == UserStatus.PENDING_ACTIVATION)
                .filter(u -> u.getStatus() != UserStatus.BLOCKED && u.getStatus() != UserStatus.DELETED)
                .toList();

        AppUserDto passwordMatch = activeUsers.stream()
                .filter(user -> identityService.verifyPassword(req.getPassword(), user.getPasswordHash()))
                .findFirst()
                .orElse(null);
        if (passwordMatch == null) {
            throw new AuthException(ErrorCode.INVALID_CREDENTIALS);
        }

        AppUserDto canonicalUser = activeUsers.stream()
                .filter(user -> user.getRole() == AppRole.CUSTOMER)
                .findFirst()
                .orElse(passwordMatch);
        return loginSingleRole(canonicalUser, List.of());
    }

    private AuthSessionResponse loginSingleRole(AppUserDto user, List<String> allRoles) {
        identityService.recordLogin(user.getId());

        BusinessRegistrationResult bizResult = resolveBusiness(user);

        if (user.getMustChangePassword()) {
            Long ttl = identityService.staffActivationSessionTtl();
            AuthSessionDto session = identityService.createSession(user.getId(), "ROLE_USER", false, ttl, true);
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

        String authority = "ROLE_USER";
        AuthSessionDto session = identityService.createSession(user.getId(), authority, false);
        return buildSessionResponse(session, user, bizResult, allRoles);
    }

    private BusinessRegistrationResult resolveBusiness(AppUserDto user) {
        BusinessRegistrationResult bizResult = businessService.findByOwner(user.getId());
        if (bizResult != null) {
            return bizResult;
        }
        return businessService.findByMember(user.getId());
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

        AuthSessionDto session = identityService.createSession(user.getId(), "ROLE_USER", false);
        BusinessRegistrationResult bizResult = resolveBusiness(user);
        List<String> allRoles = identityService.findAllByEmail(user.getEmail()).stream()
                .filter(u -> u.getStatus() == UserStatus.ACTIVE)
                .map(u -> u.getRole().name())
                .distinct()
                .toList();
        return buildSessionResponse(session, user, bizResult, allRoles);
    }

    private AuthSessionResponse buildSessionResponse(AuthSessionDto session, AppUserDto user,
                                                      BusinessRegistrationResult bizResult, List<String> allRoles) {
        AuthSessionResponse.AuthSessionResponseBuilder builder = AuthSessionResponse.builder()
                .tokenType("Bearer")
                .accessToken(jwtTokenService.issue(
                        new AskPrincipal(user.getId(), session.getId(), user.getDisplayName(), session.getAuthority()),
                        session.getExpiresAt()))
                .expiresIn(Math.max(0L, Duration.between(Instant.now(), session.getExpiresAt()).getSeconds()))
                .expiresAt(session.getExpiresAt())
                .remembered(session.getRemembered())
                .activationRequired(session.getActivationRequired())
                .role(session.getAuthority())
                .startRoute(resolveStartRoute(session.getAuthority(), bizResult))
                .user(buildUserResponse(user))
                .allRoles(allRoles);
        sessionCapabilitiesProcessor.apply(builder, user);

        if (bizResult != null) {
            var business = AuthBusinessContextResponse.builder()
                    .businessId(bizResult.getBusiness().getId())
                    .businessName(bizResult.getBusiness().getName())
                    .membershipId(bizResult.getMember().getId())
                    .memberRole(bizResult.getMember().getRole());
            if (bizResult.getBranch() != null) {
                business.branchId(bizResult.getBranch().getId())
                        .branchName(bizResult.getBranch().getName());
            }
            builder.business(business.build());
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

    private String resolveStartRoute(String authority, BusinessRegistrationResult bizResult) {
        if (bizResult != null) {
            return "BUSINESS_CABINET";
        }
        return "CLIENT_SEARCH";
    }

}

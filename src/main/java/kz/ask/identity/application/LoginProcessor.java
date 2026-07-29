package kz.ask.identity.application;

import java.util.List;
import java.time.Duration;
import java.time.Instant;
import kz.ask.business.core.domain.BusinessService;
import kz.ask.business.core.domain.dto.BusinessRegistrationResult;
import kz.ask.identity.api.dto.AuthBusinessContextResponse;
import kz.ask.identity.api.dto.AuthSessionResponse;
import kz.ask.identity.api.dto.AuthUserResponse;
import kz.ask.identity.api.dto.ChangeTemporaryPasswordRequest;
import kz.ask.identity.api.dto.LoginRequest;
import kz.ask.identity.domain.IdentityService;
import kz.ask.identity.domain.dto.AppUserDto;
import kz.ask.identity.domain.dto.VerificationDto;
import kz.ask.identity.domain.dto.AuthSessionDto;
import kz.ask.identity.authorization.domain.enums.Role;
import kz.ask.identity.domain.enums.VerificationChannel;
import kz.ask.identity.domain.enums.VerificationPurpose;
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

        return loginSingleRole(passwordMatch);
    }

    private AuthSessionResponse loginSingleRole(AppUserDto user) {
        BusinessRegistrationResult bizResult = resolveBusiness(user);

        if (user.getIsPasswordChangeRequired()) {
            identityService.recordLogin(user.getId());
            Long ttl = identityService.staffActivationSessionTtl();
            AuthSessionDto session = identityService.createSession(user.getId(), "ROLE_USER", false, ttl, true);
            return buildSessionResponse(session, user, bizResult);
        }

        if (Boolean.TRUE.equals(user.getIsTwoFactorEnabled())) {
            VerificationDto challenge = identityService.createVerification(
                    user.getId(), user.getEmail(),
                    VerificationChannel.EMAIL, VerificationPurpose.LOGIN,
                    false, null);
            emailSender.sendCode(user.getEmail(), challenge.getCodePlain());
            return AuthSessionResponse.builder()
                    .requiresTwoFactor(true)
                    .verificationId(challenge.getId())
                    .user(buildUserResponse(user))
                    .allRoles(sessionCapabilitiesProcessor.resolveAllRoles(user))
                    .build();
        }

        identityService.recordLogin(user.getId());
        String authority = resolveAuthority(user.getRole());
        AuthSessionDto session = identityService.createSession(user.getId(), authority, false);
        return buildSessionResponse(session, user, bizResult);
    }

    private String resolveAuthority(Role role) {
        return switch (role) {
            case SUPER_ADMIN -> "ROLE_SUPER_ADMIN";
            case ADMIN -> "ROLE_ADMIN";
            case MODERATOR -> "ROLE_MODERATOR";
            default -> "ROLE_USER";
        };
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
        if (user == null || !user.getIsPasswordChangeRequired()) {
            throw new ValidationException(ErrorCode.PASSWORD_CHANGE_NOT_REQUIRED);
        }

        identityService.activateStaff(user.getId(), req.getNewPassword());
        identityService.logout(principal.getUserId());

        AuthSessionDto session = identityService.createSession(user.getId(), "ROLE_USER", false);
        BusinessRegistrationResult bizResult = resolveBusiness(user);
        return buildSessionResponse(session, user, bizResult);
    }

    private AuthSessionResponse buildSessionResponse(AuthSessionDto session, AppUserDto user,
                                                      BusinessRegistrationResult bizResult) {
        AuthSessionResponse.AuthSessionResponseBuilder builder = AuthSessionResponse.builder()
                .tokenType("Bearer")
                .isTwoFactorEnabled(Boolean.TRUE.equals(user.getIsTwoFactorEnabled()))
                .accessToken(jwtTokenService.issue(
                        new AskPrincipal(user.getId(), session.getId(), user.getDisplayName(), session.getAuthority()),
                        session.getExpiresAt()))
                .expiresIn(Math.max(0L, Duration.between(Instant.now(), session.getExpiresAt()).getSeconds()))
                .expiresAt(session.getExpiresAt())
                .isRemembered(session.getIsRemembered())
                .isActivationRequired(session.getIsActivationRequired())
                .role(session.getAuthority())
                .startRoute(resolveStartRoute())
                .user(buildUserResponse(user));
        sessionCapabilitiesProcessor.apply(builder, user);

        if (bizResult != null) {
            var business = AuthBusinessContextResponse.builder()
                    .businessId(bizResult.getBusiness().getId())
                    .businessName(bizResult.getBusiness().getName())
                    .businessCategoryId(bizResult.getBusiness().getCategoryId())
                    .businessCategoryName(bizResult.getBusiness().getCategoryName())
                    .businessScope(bizResult.getBusiness().getScope())
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
                .phone(user.getPhone())
                .status(user.getStatus().name())
                .build();
    }

    private String resolveStartRoute() {
        return "CLIENT_SEARCH";
    }

}

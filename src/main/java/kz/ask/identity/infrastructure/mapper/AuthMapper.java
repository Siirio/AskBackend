package kz.ask.identity.infrastructure.mapper;

import kz.ask.business.domain.BusinessService.BusinessRegistrationResult;
import kz.ask.identity.api.dto.AuthBusinessContextResponse;
import kz.ask.identity.api.dto.AuthChallengeResponse;
import kz.ask.identity.api.dto.AuthSessionResponse;
import kz.ask.identity.api.dto.AuthUserResponse;
import kz.ask.identity.domain.entity.AppUser;
import kz.ask.identity.domain.entity.AuthChallenge;
import kz.ask.identity.domain.entity.AuthSession;
import kz.ask.identity.domain.enums.AppRole;
import kz.ask.identity.domain.enums.AuthChallengeChannel;
import kz.ask.identity.domain.enums.AuthChallengePurpose;
import kz.ask.identity.domain.enums.UserStatus;
import org.springframework.stereotype.Component;

@Component
public class AuthMapper {

    public AppUser toAppUserEntity(String email, String phone, String displayName,
                                    String passwordHash, AppRole role, UserStatus status) {
        AppUser user = new AppUser();
        user.setEmail(email);
        user.setPhone(phone);
        user.setDisplayName(displayName);
        user.setPasswordHash(passwordHash);
        user.setRole(role);
        user.setStatus(status);
        user.setMustChangePassword(false);
        return user;
    }

    public AppUser toStaffUserEntity(String email, String displayName, String passwordHash,
                                      String tempPasswordEncrypted) {
        AppUser user = new AppUser();
        user.setEmail(email);
        user.setDisplayName(displayName);
        user.setPasswordHash(passwordHash);
        user.setRole(AppRole.BUSINESS);
        user.setStatus(UserStatus.PENDING_ACTIVATION);
        user.setMustChangePassword(true);
        user.setTempPasswordEncrypted(tempPasswordEncrypted);
        return user;
    }

    public AuthChallenge toChallengeEntity(AppUser user, String email, String phone,
                                           AuthChallengeChannel channel,
                                           AuthChallengePurpose purpose, String codeHash,
                                           Integer maxAttempts, Integer challengeTtlSeconds,
                                           Boolean rememberMe, String registrationData) {
        AuthChallenge challenge = new AuthChallenge();
        challenge.setUser(user);
        challenge.setEmail(email);
        challenge.setPhone(phone);
        challenge.setChannel(channel);
        challenge.setPurpose(purpose);
        challenge.setCodeHash(codeHash);
        challenge.setAttempts(0);
        challenge.setMaxAttempts(maxAttempts);
        challenge.setExpiresAt(java.time.Instant.now().plusSeconds(challengeTtlSeconds));
        challenge.setStatus(kz.ask.identity.domain.enums.AuthChallengeStatus.PENDING);
        challenge.setRememberMe(rememberMe);
        challenge.setRegistrationData(registrationData);
        return challenge;
    }

    public AuthSession toSessionEntity(AppUser user, String tokenHash, String authority,
                                        Boolean remembered, java.time.Instant expiresAt,
                                        Boolean activationRequired) {
        AuthSession session = new AuthSession();
        session.setUser(user);
        session.setTokenHash(tokenHash);
        session.setAuthority(authority);
        session.setRemembered(remembered);
        session.setExpiresAt(expiresAt);
        session.setActivationRequired(activationRequired);
        return session;
    }

    public AuthChallengeResponse toChallengeResponse(AuthChallenge challenge,
                                                      String maskedDestination, String role) {
        return AuthChallengeResponse.builder()
                .authChallengeId(challenge.getId())
                .role(role)
                .purpose(challenge.getPurpose().name())
                .channel(challenge.getChannel().name())
                .maskedDestination(maskedDestination)
                .expiresAt(challenge.getExpiresAt())
                .build();
    }

    public AuthSessionResponse toSessionResponse(AuthSession session, AppUser user,
                                                  BusinessRegistrationResult bizResult) {
        AuthSessionResponse.AuthSessionResponseBuilder builder = AuthSessionResponse.builder()
                .tokenType("Bearer")
                .user(toUserResponse(user));

        if (session != null) {
            builder.accessToken(session.getPlainToken())
                   .expiresAt(session.getExpiresAt())
                   .remembered(session.getRemembered())
                   .activationRequired(session.getActivationRequired())
                   .role(session.getAuthority())
                   .startRoute(resolveStartRoute(session.getAuthority(), null, user));
        } else {
            builder.role(user.getRole().name())
                   .startRoute(resolveStartRoute(null, bizResult, user));
        }

        if (bizResult != null) {
            builder.business(toBusinessContextResponse(bizResult));
        }

        return builder.build();
    }

    public AuthUserResponse toUserResponse(AppUser user) {
        return AuthUserResponse.builder()
                .userId(user.getId())
                .displayName(user.getDisplayName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .status(user.getStatus().name())
                .build();
    }

    private String resolveStartRoute(String authority, BusinessRegistrationResult bizResult, AppUser user) {
        if (user.getRole() == AppRole.CUSTOMER) {
            return "CLIENT_SEARCH";
        }
        if (authority != null) {
            if (authority.contains("OWNER")) {
                return "OWNER_BRANCHES";
            }
            if (authority.contains("STAFF")) {
                return "BRANCH_WORKSPACE";
            }
        }
        if (bizResult != null) {
            return "OWNER_BRANCHES";
        }
        return "BRANCH_WORKSPACE";
    }

    public AuthBusinessContextResponse toBusinessContextResponse(BusinessRegistrationResult bizResult) {
        return AuthBusinessContextResponse.builder()
                .businessId(bizResult.business().getId())
                .businessName(bizResult.business().getName())
                .branchId(bizResult.branch().getId())
                .branchName(bizResult.branch().getName())
                .membershipId(bizResult.member().getId())
                .memberRole(bizResult.member().getRole().name())
                .build();
    }
}

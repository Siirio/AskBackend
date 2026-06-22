package kz.ask.identity.infrastructure.mapper;

import kz.ask.identity.domain.dto.AppUserDto;
import kz.ask.identity.domain.dto.AuthChallengeDto;
import kz.ask.identity.domain.dto.AuthSessionDto;
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

    public AppUserDto toAppUserDto(AppUser entity) {
        return AppUserDto.builder()
                .id(entity.getId())
                .email(entity.getEmail())
                .phone(entity.getPhone())
                .displayName(entity.getDisplayName())
                .passwordHash(entity.getPasswordHash())
                .role(entity.getRole())
                .status(entity.getStatus())
                .mustChangePassword(entity.getMustChangePassword())
                .tempPasswordEncrypted(entity.getTempPasswordEncrypted())
                .activatedAt(entity.getActivatedAt())
                .build();
    }

    public AuthChallengeDto toAuthChallengeDto(AuthChallenge entity) {
        return AuthChallengeDto.builder()
                .id(entity.getId())
                .userId(entity.getUser() != null ? entity.getUser().getId() : null)
                .email(entity.getEmail())
                .phone(entity.getPhone())
                .channel(entity.getChannel())
                .purpose(entity.getPurpose())
                .codeHash(entity.getCodeHash())
                .attempts(entity.getAttempts())
                .maxAttempts(entity.getMaxAttempts())
                .expiresAt(entity.getExpiresAt())
                .status(entity.getStatus())
                .rememberMe(entity.getRememberMe())
                .registrationData(entity.getRegistrationData())
                .codePlain(entity.getCodePlain())
                .build();
    }

    public AuthSessionDto toAuthSessionDto(AuthSession entity) {
        return AuthSessionDto.builder()
                .id(entity.getId())
                .userId(entity.getUser() != null ? entity.getUser().getId() : null)
                .userDisplayName(entity.getUser() != null ? entity.getUser().getDisplayName() : null)
                .tokenHash(entity.getTokenHash())
                .authority(entity.getAuthority())
                .remembered(entity.getRemembered())
                .activationRequired(entity.getActivationRequired())
                .expiresAt(entity.getExpiresAt())
                .revokedAt(entity.getRevokedAt())
                .plainToken(entity.getPlainToken())
                .build();
    }
}

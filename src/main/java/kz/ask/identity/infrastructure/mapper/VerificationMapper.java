package kz.ask.identity.infrastructure.mapper;

import kz.ask.identity.domain.dto.AppUserDto;
import kz.ask.identity.domain.dto.VerificationDto;
import kz.ask.identity.domain.dto.AuthSessionDto;
import kz.ask.identity.domain.entity.AppUser;
import kz.ask.identity.domain.entity.Verification;
import kz.ask.identity.domain.entity.AuthSession;
import kz.ask.identity.authorization.domain.enums.Role;
import kz.ask.identity.domain.enums.VerificationChannel;
import kz.ask.identity.domain.enums.VerificationPurpose;
import kz.ask.identity.domain.enums.UserStatus;
import org.springframework.stereotype.Component;

@Component
public class VerificationMapper {

    public AppUser toAppUserEntity(String email, String displayName,
                                    String passwordHash, Role role, UserStatus status) {
        AppUser user = new AppUser();
        user.setEmail(email);
        user.setDisplayName(displayName);
        user.setPasswordHash(passwordHash);
        user.setRole(role);
        user.setStatus(status);
        user.setIsPasswordChangeRequired(false);
        user.setIsTwoFactorEnabled(false);
        return user;
    }

    public AppUser toStaffUserEntity(String email, String displayName, String passwordHash,
                                      String tempPasswordEncrypted, Role role) {
        AppUser user = new AppUser();
        user.setEmail(email);
        user.setDisplayName(displayName);
        user.setPasswordHash(passwordHash);
        user.setRole(role);
        user.setStatus(UserStatus.PENDING_ACTIVATION);
        user.setIsPasswordChangeRequired(true);
        user.setTempPasswordEncrypted(tempPasswordEncrypted);
        user.setIsTwoFactorEnabled(false);
        return user;
    }

    public Verification toVerificationEntity(AppUser user, String email,
                                           VerificationChannel channel,
                                           VerificationPurpose purpose, String codeHash,
                                           Integer maxAttempts, Integer challengeTtlSeconds,
                                           Boolean rememberMe, String registrationData) {
        Verification verification = new Verification();
        verification.setUser(user);
        verification.setEmail(email);
        verification.setChannel(channel);
        verification.setPurpose(purpose);
        verification.setCodeHash(codeHash);
        verification.setAttempts(0);
        verification.setMaxAttempts(maxAttempts);
        verification.setExpiresAt(java.time.Instant.now().plusSeconds(challengeTtlSeconds));
        verification.setStatus(kz.ask.identity.domain.enums.VerificationStatus.PENDING);
        verification.setIsRememberMe(rememberMe);
        verification.setRegistrationData(registrationData);
        return verification;
    }

    public AuthSession toSessionEntity(AppUser user, String tokenHash, String authority,
                                        Boolean remembered, java.time.Instant expiresAt,
                                        Boolean activationRequired) {
        AuthSession session = new AuthSession();
        session.setUser(user);
        session.setTokenHash(tokenHash);
        session.setAuthority(authority);
        session.setIsRemembered(remembered);
        session.setExpiresAt(expiresAt);
        session.setIsActivationRequired(activationRequired);
        return session;
    }

    public AppUserDto toAppUserDto(AppUser entity) {
        return AppUserDto.builder()
                .id(entity.getId())
                .email(entity.getEmail())
                .displayName(entity.getDisplayName())
                .passwordHash(entity.getPasswordHash())
                .role(entity.getRole())
                .status(entity.getStatus())
                .isPasswordChangeRequired(entity.getIsPasswordChangeRequired())
                .isTwoFactorEnabled(entity.getIsTwoFactorEnabled())
                .tempPasswordEncrypted(entity.getTempPasswordEncrypted())
                .activatedAt(entity.getActivatedAt())
                .lastLoginAt(entity.getLastLoginAt())
                .build();
    }

    public VerificationDto toVerificationDto(Verification entity) {
        return VerificationDto.builder()
                .id(entity.getId())
                .userId(entity.getUser() != null ? entity.getUser().getId() : null)
                .email(entity.getEmail())
                .channel(entity.getChannel())
                .purpose(entity.getPurpose())
                .codeHash(entity.getCodeHash())
                .attempts(entity.getAttempts())
                .maxAttempts(entity.getMaxAttempts())
                .expiresAt(entity.getExpiresAt())
                .status(entity.getStatus())
                .isRememberMe(entity.getIsRememberMe())
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
                .isRemembered(entity.getIsRemembered())
                .isActivationRequired(entity.getIsActivationRequired())
                .expiresAt(entity.getExpiresAt())
                .revokedAt(entity.getRevokedAt())
                .plainToken(entity.getPlainToken())
                .build();
    }
}

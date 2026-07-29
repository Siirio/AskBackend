package kz.ask.identity.domain;

import java.util.List;
import java.util.UUID;
import kz.ask.identity.domain.dto.AppUserDto;
import kz.ask.identity.domain.dto.VerificationDto;
import kz.ask.identity.domain.dto.AuthSessionDto;
import kz.ask.identity.authorization.domain.enums.Role;
import kz.ask.identity.domain.enums.VerificationChannel;
import kz.ask.identity.domain.enums.VerificationPurpose;

public interface IdentityService {

    AppUserDto createUser(String email, String displayName, String password, Role role);

    AppUserDto createStaffUser(String email, String displayName, String tempPassword, Role role);

    VerificationDto createVerification(UUID userId, String email,
                                     VerificationChannel channel,
                                     VerificationPurpose purpose,
                                     Boolean rememberMe,
                                     String registrationData);

    VerificationDto verifyCode(
            UUID challengeId,
            String code,
            UUID expectedUserId,
            VerificationPurpose... allowedPurposes);

    void cancelVerification(UUID challengeId);

    void clearChallengeRegistrationData(UUID challengeId);

    AuthSessionDto createSession(UUID userId, String authority, Boolean remembered);

    AuthSessionDto createSession(UUID userId, String authority, Boolean remembered, Long ttlSeconds, Boolean activationRequired);

    AuthSessionDto findSessionByToken(String token);

    AuthSessionDto findSessionById(UUID sessionId);

    void activateUser(UUID userId);

    void activateStaff(UUID userId, String newPassword);

    void resetStaffPassword(UUID userId, String newTempPassword);

    String revealTemporaryPassword(UUID userId);

    void deletePendingStaffUser(UUID userId);

    void updateUserStatus(UUID userId, String status);

    void logout(UUID userId);

    AppUserDto findById(UUID id);

    List<AppUserDto> findAllByEmail(String email);

    List<AppUserDto> findAllActiveByEmail(String email);

    Boolean emailExists(String email);

    Boolean verifyPassword(String rawPassword, String encodedPassword);

    String encodePassword(String password);

    String maskEmail(String email);

    Long staffSessionTtl(Boolean remembered);

    Long staffActivationSessionTtl();

    void updatePendingUserCredentials(UUID userId, String email, String displayName, String password);

    void updateProfile(UUID userId, String displayName, String email, String phone);

    void updateEmail(UUID userId, String email);

    void changePassword(UUID userId, String newPassword);

    void changePasswordHash(UUID userId, String passwordHash);

    void revokeOtherSessions(UUID userId, UUID currentSessionId);

    void setTwoFactorEnabled(UUID userId, Boolean enabled);

    Boolean isTwoFactorEnabled(UUID userId);

    void recordLogin(UUID userId);

    void anonymizeAccount(UUID userId);
}

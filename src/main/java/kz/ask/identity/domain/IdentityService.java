package kz.ask.identity.domain;

import java.util.List;
import java.util.UUID;
import kz.ask.identity.domain.dto.AppUserDto;
import kz.ask.identity.domain.dto.AuthChallengeDto;
import kz.ask.identity.domain.dto.AuthSessionDto;
import kz.ask.identity.domain.enums.AppRole;
import kz.ask.identity.domain.enums.AuthChallengeChannel;
import kz.ask.identity.domain.enums.AuthChallengePurpose;

public interface IdentityService {

    AppUserDto createUser(String email, String displayName, String password, AppRole role);

    AppUserDto createStaffUser(String email, String displayName, String tempPassword, AppRole role);

    AuthChallengeDto createChallenge(UUID userId, String email,
                                     AuthChallengeChannel channel,
                                     AuthChallengePurpose purpose,
                                     Boolean rememberMe,
                                     String registrationData);

    AuthChallengeDto verifyCode(UUID challengeId, String code);

    void clearChallengeRegistrationData(UUID challengeId);

    AuthSessionDto createSession(UUID userId, String authority, Boolean remembered);

    AuthSessionDto createSession(UUID userId, String authority, Boolean remembered, Long ttlSeconds, Boolean activationRequired);

    AuthSessionDto findSessionByToken(String token);

    void activateUser(UUID userId);

    void activateStaff(UUID userId, String newPassword);

    void resetStaffPassword(UUID userId, String newTempPassword);

    void updateUserStatus(UUID userId, String status);

    void logout(UUID userId);

    AppUserDto findById(UUID id);

    List<AppUserDto> findAllByEmail(String email);

    List<AppUserDto> findAllActiveByEmail(String email);

    AppUserDto findByEmailAndRole(String email, AppRole role);

    Boolean emailExists(String email);

    Boolean emailExistsForRole(String email, AppRole role);

    Boolean verifyPassword(String rawPassword, String encodedPassword);

    String maskEmail(String email);

    Long staffSessionTtl(Boolean remembered);

    Long staffActivationSessionTtl();

    void updateProfile(UUID userId, String displayName, String email);

    void updateEmail(UUID userId, String email);

    void changePassword(UUID userId, String newPassword);

    void toggleTwoFactor(UUID userId);

    Boolean isTwoFactorEnabled(UUID userId);

    void recordLogin(UUID userId);

    void anonymizeAccount(UUID userId);
}

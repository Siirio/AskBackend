package kz.ask.identity.domain;

import java.util.UUID;
import kz.ask.identity.domain.dto.AppUserDto;
import kz.ask.identity.domain.dto.AuthChallengeDto;
import kz.ask.identity.domain.dto.AuthSessionDto;
import kz.ask.identity.domain.enums.AppRole;
import kz.ask.identity.domain.enums.AuthChallengeChannel;
import kz.ask.identity.domain.enums.AuthChallengePurpose;

public interface IdentityService {

    AppUserDto createUser(String email, String displayName, String password, AppRole role);

    AppUserDto createStaffUser(String email, String displayName, String tempPassword);

    AuthChallengeDto createChallenge(UUID userId, String email,
                                     AuthChallengeChannel channel,
                                     AuthChallengePurpose purpose,
                                     Boolean rememberMe,
                                     String registrationData);

    AuthChallengeDto verifyCode(UUID challengeId, String code);

    AuthSessionDto createSession(UUID userId, String authority, Boolean remembered);

    AuthSessionDto createSession(UUID userId, String authority, Boolean remembered, Long ttlSeconds, Boolean activationRequired);

    AuthSessionDto findSessionByToken(String token);

    void activateUser(UUID userId);

    void activateStaff(UUID userId, String newPassword);

    void resetStaffPassword(UUID userId, String newTempPassword);

    void updateUserStatus(UUID userId, String status);

    void logout(UUID userId);

    AppUserDto findById(UUID id);

    AppUserDto findActiveByEmail(String email);

    AppUserDto findByEmail(String email);

    AppUserDto findByEmailAndRole(String email, AppRole role);

    Boolean emailExists(String email);

    Boolean emailExistsForRole(String email, AppRole role);

    Boolean verifyPassword(String rawPassword, String encodedPassword);

    String maskEmail(String email);

    Long staffSessionTtl(Boolean remembered);

    Long staffActivationSessionTtl();

    void updateProfile(UUID userId, String displayName, String email);
}

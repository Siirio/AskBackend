package kz.ask.identity.domain;

import java.util.UUID;
import kz.ask.identity.domain.dto.AppUserDto;
import kz.ask.identity.domain.dto.AuthChallengeDto;
import kz.ask.identity.domain.dto.AuthSessionDto;
import kz.ask.identity.domain.enums.AppRole;
import kz.ask.identity.domain.enums.AuthChallengeChannel;
import kz.ask.identity.domain.enums.AuthChallengePurpose;

public interface IdentityService {

    AppUserDto createUser(String email, String phone, String displayName, String password, AppRole role);

    AppUserDto createStaffUser(String email, String displayName, String tempPassword);

    AuthChallengeDto createChallenge(UUID userId, String email, String phone,
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

    AppUserDto findActiveByPhone(String phone);

    AppUserDto findByEmail(String email);

    Boolean emailExists(String email);

    Boolean phoneExists(String phone);

    Boolean verifyPassword(String rawPassword, String encodedPassword);

    String maskEmail(String email);

    String maskPhone(String phone);

    Long staffSessionTtl(Boolean remembered);

    Long staffActivationSessionTtl();
}

package kz.ask.identity.domain;

import java.util.UUID;
import kz.ask.identity.domain.entity.AppUser;
import kz.ask.identity.domain.entity.AuthChallenge;
import kz.ask.identity.domain.entity.AuthSession;
import kz.ask.identity.domain.enums.AppRole;
import kz.ask.identity.domain.enums.AuthChallengeChannel;
import kz.ask.identity.domain.enums.AuthChallengePurpose;

public interface IdentityService {

    AppUser createUser(String email, String phone, String displayName, String password, AppRole role);

    AppUser createStaffUser(String email, String displayName, String tempPassword);

    AuthChallenge createChallenge(AppUser user, String email, String phone,
                                  AuthChallengeChannel channel,
                                  AuthChallengePurpose purpose,
                                  Boolean rememberMe,
                                  String registrationData);

    AuthChallenge verifyCode(UUID challengeId, String code);

    AuthSession createSession(AppUser user, String authority, Boolean remembered);

    AuthSession createSession(AppUser user, String authority, Boolean remembered, Long ttlSeconds, Boolean activationRequired);

    AuthSession findSessionByToken(String token);

    void activateUser(AppUser user);

    void activateStaff(AppUser user, String newPassword);

    void resetStaffPassword(AppUser user, String newTempPassword);

    void logout(UUID userId);

    AppUser findById(UUID id);

    AppUser findActiveByEmail(String email);

    AppUser findActiveByPhone(String phone);

    AppUser findByEmail(String email);

    Boolean emailExists(String email);

    Boolean phoneExists(String phone);

    Boolean verifyPassword(String rawPassword, String encodedPassword);

    String maskEmail(String email);

    String maskPhone(String phone);

    Long staffSessionTtl(Boolean remembered);

    Long staffActivationSessionTtl();
}

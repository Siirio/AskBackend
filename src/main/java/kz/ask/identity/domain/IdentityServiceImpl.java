package kz.ask.identity.domain;

import jakarta.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Arrays;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import kz.ask.identity.domain.dto.AppUserDto;
import kz.ask.identity.domain.dto.VerificationDto;
import kz.ask.identity.domain.dto.AuthSessionDto;
import kz.ask.identity.domain.entity.AppUser;
import kz.ask.identity.domain.entity.Verification;
import kz.ask.identity.domain.entity.AuthSession;
import kz.ask.identity.domain.entity.CustomerProfile;
import kz.ask.identity.authorization.domain.enums.Role;
import kz.ask.identity.domain.enums.VerificationChannel;
import kz.ask.identity.domain.enums.VerificationPurpose;
import kz.ask.identity.domain.enums.VerificationStatus;
import kz.ask.identity.domain.enums.UserStatus;
import kz.ask.identity.infrastructure.mapper.VerificationMapper;
import kz.ask.shared.error.ErrorCode;
import kz.ask.shared.error.InternalServerException;
import kz.ask.shared.error.NotFoundException;
import kz.ask.shared.error.ValidationException;
import kz.ask.identity.infrastructure.repository.AppUserRepository;
import kz.ask.identity.infrastructure.repository.VerificationRepository;
import kz.ask.identity.infrastructure.repository.AuthSessionRepository;
import kz.ask.identity.infrastructure.repository.CustomerProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class IdentityServiceImpl implements IdentityService {

    private final AppUserRepository appUserRepository;
    private final VerificationRepository verificationRepository;
    private final AuthSessionRepository authSessionRepository;
    private final CustomerProfileRepository customerProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final VerificationMapper verificationMapper;
    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${auth.challenge.ttl}")
    private Integer challengeTtlSeconds;
    @Value("${auth.challenge.code-length}")
    private Integer challengeCodeLength;
    @Value("${auth.challenge.max-attempts}")
    private Integer challengeMaxAttempts;
    @Value("${auth.verification.staging-bypass:false}")
    private Boolean stagingBypass;
    @Value("${auth.customer.session.ttl}")
    private Long customerSessionTtlSeconds;
    @Value("${auth.customer.remembered-session.ttl}")
    private Long customerRememberedSessionTtlSeconds;
    @Value("${auth.business.session.ttl}")
    private Long businessSessionTtlSeconds;
    @Value("${auth.business.remembered-session.ttl}")
    private Long businessRememberedSessionTtlSeconds;
    @Value("${auth.staff.session.ttl}")
    private Long staffSessionTtlSeconds;
    @Value("${auth.staff.remembered-session.ttl}")
    private Long staffRememberedSessionTtlSeconds;
    @Value("${auth.staff.activation-session.ttl}")
    private Long staffActivationSessionTtlSeconds;

    @Value("${auth.staff.temp-password-key}")
    private String tempPasswordKey;
    @Value("${auth.account.deleted-display-name:Deleted user}")
    private String deletedDisplayName;
    private SecretKeySpec aesKey;

    @PostConstruct
    private void initAesKey() {
        byte[] keyBytes = sha256Raw(tempPasswordKey);
        this.aesKey = new SecretKeySpec(keyBytes, "AES");
    }

    @Override
    @Transactional
    public AppUserDto createUser(String email, String displayName, String password, Role role) {
        AppUser user = verificationMapper.toAppUserEntity(
                blankToNull(email), displayName,
                hashPassword(password), role, UserStatus.PENDING);
        AppUser saved = appUserRepository.save(user);
        return verificationMapper.toAppUserDto(saved);
    }

    @Override
    @Transactional
    public VerificationDto createVerification(UUID userId, String email,
                                            VerificationChannel channel,
                                            VerificationPurpose purpose,
                                            Boolean rememberMe,
                                            String registrationData) {
        AppUser user = userId != null ? appUserRepository.getReferenceById(userId) : null;
        if (user != null) {
            expireUserPendingChallenges(user, purpose);
        }
        String code = generateCode();
        Verification verification = verificationMapper.toVerificationEntity(
                user, email, channel, purpose,
                hashCode(code), challengeMaxAttempts, challengeTtlSeconds,
                Boolean.TRUE.equals(rememberMe), registrationData);
        Verification saved = verificationRepository.save(verification);
        saved.setCodePlain(code);
        return verificationMapper.toVerificationDto(saved);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, noRollbackFor = ValidationException.class)
    public VerificationDto verifyCode(
            UUID challengeId,
            String code,
            UUID expectedUserId,
            VerificationPurpose... allowedPurposes) {
        Verification verification = verificationRepository.findByIdAndStatus(challengeId, VerificationStatus.PENDING)
                .orElseThrow(() -> new NotFoundException(ErrorCode.CHALLENGE_NOT_FOUND, challengeId));
        if ((expectedUserId != null
                && (verification.getUser() == null || !expectedUserId.equals(verification.getUser().getId())))
                || Arrays.stream(allowedPurposes).noneMatch(purpose -> purpose == verification.getPurpose())) {
            throw new ValidationException(ErrorCode.CHALLENGE_INVALID_CODE, challengeId);
        }
        if (verification.getExpiresAt().isBefore(Instant.now())) {
            verification.setStatus(VerificationStatus.EXPIRED);
            throw new ValidationException(ErrorCode.CHALLENGE_EXPIRED, challengeId);
        }
        if (verification.getAttempts() >= verification.getMaxAttempts()) {
            verification.setStatus(VerificationStatus.FAILED);
            throw new ValidationException(ErrorCode.CHALLENGE_MAX_ATTEMPTS, challengeId);
        }
        verification.setAttempts(verification.getAttempts() + 1);
        if (!Boolean.TRUE.equals(stagingBypass) && !verifyCodeHash(code, verification.getCodeHash())) {
            if (verification.getAttempts() >= verification.getMaxAttempts()) {
                verification.setStatus(VerificationStatus.FAILED);
            }
            throw new ValidationException(ErrorCode.CHALLENGE_INVALID_CODE, challengeId);
        }
        verification.setStatus(VerificationStatus.VERIFIED);
        return verificationMapper.toVerificationDto(verification);
    }

    @Override
    @Transactional
    public void cancelVerification(UUID challengeId) {
        Verification verification = verificationRepository.findById(challengeId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.CHALLENGE_NOT_FOUND, challengeId));
        if (verification.getStatus() != VerificationStatus.PENDING) {
            return;
        }
        verification.setStatus(VerificationStatus.EXPIRED);
    }

    @Override
    @Transactional
    public void clearChallengeRegistrationData(UUID challengeId) {
        Verification verification = verificationRepository.findById(challengeId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.CHALLENGE_NOT_FOUND, challengeId));
        verification.setRegistrationData(null);
    }

    @Override
    @Transactional
    public AuthSessionDto createSession(UUID userId, String authority, Boolean remembered) {
        Long ttl = sessionTtl(authority, remembered);
        return createSession(userId, authority, remembered, ttl, false);
    }

    @Override
    @Transactional
    public AuthSessionDto findSessionByToken(String token) {
        String hash = hashToken(token);
        AuthSession session = authSessionRepository.findByTokenHash(hash).orElse(null);
        return activeSession(session);
    }

    @Override
    @Transactional
    public AuthSessionDto findSessionById(UUID sessionId) {
        AuthSession session = authSessionRepository.findById(sessionId).orElse(null);
        return activeSession(session);
    }

    private AuthSessionDto activeSession(AuthSession session) {
        if (session == null || session.getRevokedAt() != null) {
            return null;
        }
        if (session.getExpiresAt().isBefore(Instant.now())) {
            session.setRevokedAt(Instant.now());
            return null;
        }
        return verificationMapper.toAuthSessionDto(session);
    }

    @Override
    @Transactional
    public void activateUser(UUID userId) {
        AppUser user = appUserRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND, userId));
        user.setStatus(UserStatus.ACTIVE);
        appUserRepository.saveAndFlush(user);
        if (!customerProfileRepository.findByUserId(userId).isPresent()) {
            CustomerProfile profile = new CustomerProfile();
            profile.setUser(user);
            customerProfileRepository.save(profile);
        }
    }

    @Override
    @Transactional
    public AppUserDto createStaffUser(String email, String displayName, String tempPassword, Role role) {
        AppUser user = verificationMapper.toStaffUserEntity(
                email, displayName, hashPassword(tempPassword), encrypt(tempPassword), role);
        AppUser saved = appUserRepository.save(user);
        return verificationMapper.toAppUserDto(saved);
    }

    @Override
    @Transactional
    public AuthSessionDto createSession(UUID userId, String authority, Boolean remembered,
                                        Long ttlSeconds, Boolean activationRequired) {
        AppUser user = appUserRepository.getReferenceById(userId);
        String token = generateToken();
        AuthSession session = verificationMapper.toSessionEntity(
                user, hashToken(token), authority, remembered,
                Instant.now().plusSeconds(ttlSeconds), activationRequired);
        AuthSession saved = authSessionRepository.save(session);
        saved.setPlainToken(token);
        return verificationMapper.toAuthSessionDto(saved);
    }

    @Override
    @Transactional
    public void activateStaff(UUID userId, String newPassword) {
        AppUser user = appUserRepository.getReferenceById(userId);
        user.setPasswordHash(hashPassword(newPassword));
        user.setTempPasswordEncrypted(null);
        user.setIsPasswordChangeRequired(false);
        user.setActivatedAt(Instant.now());
        user.setStatus(UserStatus.ACTIVE);
    }

    @Override
    @Transactional
    public void resetStaffPassword(UUID userId, String newTempPassword) {
        AppUser user = appUserRepository.getReferenceById(userId);
        user.setPasswordHash(hashPassword(newTempPassword));
        user.setTempPasswordEncrypted(encrypt(newTempPassword));
        user.setIsPasswordChangeRequired(true);
        user.setStatus(UserStatus.PASSWORD_RESET_REQUIRED);
    }

    @Override
    @Transactional(readOnly = true)
    public String revealTemporaryPassword(UUID userId) {
        AppUser user = appUserRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND, userId));
        if (!Boolean.TRUE.equals(user.getIsPasswordChangeRequired())) {
            return null;
        }
        return decrypt(user.getTempPasswordEncrypted());
    }

    @Override
    @Transactional
    public void deletePendingStaffUser(UUID userId) {
        AppUser user = appUserRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND, userId));
        if (user.getStatus() != UserStatus.PENDING_ACTIVATION || user.getActivatedAt() != null) {
            throw new ValidationException(ErrorCode.STAFF_ALREADY_ACTIVATED);
        }
        authSessionRepository.deleteByUserId(userId);
        verificationRepository.deleteByUserId(userId);
        customerProfileRepository.deleteByUserId(userId);
        appUserRepository.delete(user);
    }

    @Override
    @Transactional
    public void updateUserStatus(UUID userId, String status) {
        AppUser user = appUserRepository.getReferenceById(userId);
        user.setStatus(UserStatus.valueOf(status));
    }

    @Override
    @Transactional
    public void logout(UUID userId) {
        authSessionRepository.revokeAllForUser(userId, Instant.now());
    }

    @Override
    public AppUserDto findById(UUID id) {
        return appUserRepository.findById(id)
                .map(verificationMapper::toAppUserDto)
                .orElse(null);
    }

    @Override
    public List<AppUserDto> findAllActiveByEmail(String email) {
        return appUserRepository.findAllByEmailIgnoreCase(email).stream()
                .filter(u -> u.getStatus() == UserStatus.ACTIVE)
                .map(verificationMapper::toAppUserDto)
                .toList();
    }

    @Override
    public List<AppUserDto> findAllByEmail(String email) {
        return appUserRepository.findAllByEmailIgnoreCase(email).stream()
                .map(verificationMapper::toAppUserDto)
                .toList();
    }

    @Override
    public Boolean emailExists(String email) {
        return appUserRepository.existsByEmailIgnoreCase(email);
    }

    @Override
    public String maskEmail(String email) {
        if (email == null) return null;
        int at = email.indexOf('@');
        if (at <= 2) return email;
        return email.charAt(0) + "***" + email.charAt(at - 1) + email.substring(at);
    }

    @Override
    public Long staffSessionTtl(Boolean remembered) {
        return remembered ? staffRememberedSessionTtlSeconds : staffSessionTtlSeconds;
    }

    @Override
    public Long staffActivationSessionTtl() {
        return staffActivationSessionTtlSeconds;
    }

    @Override
    @Transactional
    public void updatePendingUserCredentials(UUID userId, String email, String displayName, String password) {
        AppUser user = appUserRepository.getReferenceById(userId);
        user.setEmail(blankToNull(email));
        user.setDisplayName(displayName);
        user.setPasswordHash(hashPassword(password));
    }

    @Override
    @Transactional
    public void updateProfile(UUID userId, String displayName, String email, String phone) {
        AppUser user = appUserRepository.getReferenceById(userId);
        if (displayName != null) user.setDisplayName(displayName);
        if (phone != null) user.setPhone(blankToNull(phone));
    }

    @Override
    @Transactional
    public void updateEmail(UUID userId, String email) {
        AppUser user = appUserRepository.getReferenceById(userId);
        user.setEmail(blankToNull(email));
    }

    @Override
    @Transactional
    public void changePassword(UUID userId, String newPassword) {
        AppUser user = appUserRepository.getReferenceById(userId);
        user.setPasswordHash(hashPassword(newPassword));
    }

    @Override
    @Transactional
    public void changePasswordHash(UUID userId, String passwordHash) {
        AppUser user = appUserRepository.getReferenceById(userId);
        user.setPasswordHash(passwordHash);
    }

    @Override
    @Transactional
    public void revokeOtherSessions(UUID userId, UUID currentSessionId) {
        authSessionRepository.revokeOtherSessions(userId, currentSessionId, Instant.now());
    }

    @Override
    @Transactional
    public void setTwoFactorEnabled(UUID userId, Boolean enabled) {
        AppUser user = appUserRepository.getReferenceById(userId);
        user.setIsTwoFactorEnabled(Boolean.TRUE.equals(enabled));
    }

    @Override
    @Transactional
    public void recordLogin(UUID userId) {
        AppUser user = appUserRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND, userId));
        user.setLastLoginAt(Instant.now());
        appUserRepository.saveAndFlush(user);
    }

    @Override
    @Transactional
    public void anonymizeAccount(UUID userId) {
        AppUser user = appUserRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND, userId));
        authSessionRepository.revokeAllForUser(userId, Instant.now());
        verificationRepository.deleteByUserId(userId);
        user.setEmail(null);
        user.setDisplayName(deletedDisplayName);
        user.setPasswordHash(hashPassword(generateToken()));
        user.setStatus(UserStatus.DELETED);
        user.setIsPasswordChangeRequired(false);
        user.setIsTwoFactorEnabled(false);
        user.setTempPasswordEncrypted(null);
    }

    @Override
    public Boolean isTwoFactorEnabled(UUID userId) {
        return appUserRepository.findById(userId)
                .map(u -> Boolean.TRUE.equals(u.getIsTwoFactorEnabled()))
                .orElse(false);
    }

    private String encrypt(String plainText) {
        if (plainText == null) return null;
        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, aesKey);
            return Base64.getEncoder().encodeToString(cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new InternalServerException(ErrorCode.INTERNAL_ERROR, e);
        }
    }

    private String decrypt(String encrypted) {
        if (encrypted == null) return null;
        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, aesKey);
            return new String(cipher.doFinal(Base64.getDecoder().decode(encrypted)), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new InternalServerException(ErrorCode.INTERNAL_ERROR, e);
        }
    }

    private String generateCode() {
        int max = (int) Math.pow(10, challengeCodeLength);
        int code = secureRandom.nextInt(max);
        return String.format("%0" + challengeCodeLength + "d", code);
    }

    private String generateToken() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return HexFormat.of().formatHex(bytes);
    }

    private Long sessionTtl(String authority, Boolean remembered) {
        if (authority.contains("CUSTOMER")) {
            return remembered ? customerRememberedSessionTtlSeconds : customerSessionTtlSeconds;
        }
        return remembered ? businessRememberedSessionTtlSeconds : businessSessionTtlSeconds;
    }

    private void expireUserPendingChallenges(AppUser user, VerificationPurpose purpose) {
        if (user == null) return;
        verificationRepository.expirePendingChallengesForUser(
                user.getId(),
                purpose,
                VerificationStatus.EXPIRED,
                VerificationStatus.PENDING);
    }

    private String hashPassword(String password) {
        return passwordEncoder.encode(password);
    }

    public Boolean verifyPassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    @Override
    public String encodePassword(String password) {
        return hashPassword(password);
    }

    private String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s;
    }

    static String hashCode(String code) {
        return sha256("code:" + code);
    }

    static String hashToken(String token) {
        return sha256("tok:" + token);
    }

    static Boolean verifyCodeHash(String code, String hash) {
        return hashCode(code).equals(hash);
    }

    private static byte[] sha256Raw(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return md.digest(input.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            throw new InternalServerException(ErrorCode.INTERNAL_ERROR, e);
        }
    }

    private static String sha256(String input) {
        return HexFormat.of().formatHex(sha256Raw(input));
    }
}

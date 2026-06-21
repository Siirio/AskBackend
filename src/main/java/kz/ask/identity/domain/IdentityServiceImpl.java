package kz.ask.identity.domain;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.UUID;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import kz.ask.identity.domain.entity.AppUser;
import kz.ask.identity.domain.entity.AuthChallenge;
import kz.ask.identity.domain.entity.AuthSession;
import kz.ask.identity.domain.enums.AppRole;
import kz.ask.identity.domain.enums.AuthChallengeChannel;
import kz.ask.identity.domain.enums.AuthChallengePurpose;
import kz.ask.identity.domain.enums.AuthChallengeStatus;
import kz.ask.identity.domain.enums.UserStatus;
import kz.ask.identity.infrastructure.mapper.AuthMapper;
import kz.ask.shared.error.ErrorCode;
import kz.ask.shared.error.InternalServerException;
import kz.ask.shared.error.NotFoundException;
import kz.ask.shared.error.ValidationException;
import kz.ask.identity.infrastructure.repository.AppUserRepository;
import kz.ask.identity.infrastructure.repository.AuthChallengeRepository;
import kz.ask.identity.infrastructure.repository.AuthSessionRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class IdentityServiceImpl implements IdentityService {

    private final AppUserRepository appUserRepository;
    private final AuthChallengeRepository authChallengeRepository;
    private final AuthSessionRepository authSessionRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecureRandom secureRandom;
    private final AuthMapper authMapper;

    private final Integer challengeTtlSeconds;
    private final Integer challengeCodeLength;
    private final Integer challengeMaxAttempts;
    private final Long customerSessionTtlSeconds;
    private final Long customerRememberedSessionTtlSeconds;
    private final Long businessSessionTtlSeconds;
    private final Long businessRememberedSessionTtlSeconds;
    private final Long staffSessionTtlSeconds;
    private final Long staffRememberedSessionTtlSeconds;
    private final Long staffActivationSessionTtlSeconds;
    private final SecretKeySpec aesKey;

    public IdentityServiceImpl(
            AppUserRepository appUserRepository,
            AuthChallengeRepository authChallengeRepository,
            AuthSessionRepository authSessionRepository,
            PasswordEncoder passwordEncoder,
            AuthMapper authMapper,
            @Value("${auth.challenge.ttl}") Integer challengeTtlSeconds,
            @Value("${auth.challenge.code-length}") Integer challengeCodeLength,
            @Value("${auth.challenge.max-attempts}") Integer challengeMaxAttempts,
            @Value("${auth.customer.session.ttl}") Long customerSessionTtlSeconds,
            @Value("${auth.customer.remembered-session.ttl}") Long customerRememberedSessionTtlSeconds,
            @Value("${auth.business.session.ttl}") Long businessSessionTtlSeconds,
            @Value("${auth.business.remembered-session.ttl}") Long businessRememberedSessionTtlSeconds,
            @Value("${auth.staff.session.ttl}") Long staffSessionTtlSeconds,
            @Value("${auth.staff.remembered-session.ttl}") Long staffRememberedSessionTtlSeconds,
            @Value("${auth.staff.activation-session.ttl}") Long staffActivationSessionTtlSeconds,
            @Value("${auth.staff.temp-password-key}") String tempPasswordKey) {
        this.appUserRepository = appUserRepository;
        this.authChallengeRepository = authChallengeRepository;
        this.authSessionRepository = authSessionRepository;
        this.passwordEncoder = passwordEncoder;
        this.authMapper = authMapper;
        this.secureRandom = new SecureRandom();
        this.challengeTtlSeconds = challengeTtlSeconds;
        this.challengeCodeLength = challengeCodeLength;
        this.challengeMaxAttempts = challengeMaxAttempts;
        this.customerSessionTtlSeconds = customerSessionTtlSeconds;
        this.customerRememberedSessionTtlSeconds = customerRememberedSessionTtlSeconds;
        this.businessSessionTtlSeconds = businessSessionTtlSeconds;
        this.businessRememberedSessionTtlSeconds = businessRememberedSessionTtlSeconds;
        this.staffSessionTtlSeconds = staffSessionTtlSeconds;
        this.staffRememberedSessionTtlSeconds = staffRememberedSessionTtlSeconds;
        this.staffActivationSessionTtlSeconds = staffActivationSessionTtlSeconds;
        byte[] keyBytes = sha256Raw(tempPasswordKey);
        this.aesKey = new SecretKeySpec(keyBytes, "AES");
    }

    @Override
    @Transactional
    public AppUser createUser(String email, String phone, String displayName, String password, AppRole role) {
        AppUser user = authMapper.toAppUserEntity(
                blankToNull(email), blankToNull(phone), displayName,
                hashPassword(password), role, UserStatus.PENDING);
        return appUserRepository.save(user);
    }

    @Transactional
    public AuthChallenge createChallenge(AppUser user, String email, String phone,
                                          AuthChallengeChannel channel,
                                          AuthChallengePurpose purpose,
                                          Boolean rememberMe,
                                          String registrationData) {
        expireUserPendingChallenges(user);
        String code = generateCode();
        AuthChallenge challenge = authMapper.toChallengeEntity(
                user, email, phone, channel, purpose,
                hashCode(code), challengeMaxAttempts, challengeTtlSeconds,
                rememberMe, registrationData);
        AuthChallenge saved = authChallengeRepository.save(challenge);
        saved.setCodePlain(code);
        return saved;
    }

    @Transactional
    public AuthChallenge verifyCode(UUID challengeId, String code) {
        AuthChallenge challenge = authChallengeRepository.findByIdAndStatus(challengeId, AuthChallengeStatus.PENDING)
                .orElseThrow(() -> new NotFoundException(ErrorCode.CHALLENGE_NOT_FOUND, challengeId));
        if (challenge.getExpiresAt().isBefore(Instant.now())) {
            challenge.setStatus(AuthChallengeStatus.EXPIRED);
            throw new ValidationException(ErrorCode.CHALLENGE_EXPIRED, challengeId);
        }
        if (challenge.getAttempts() >= challenge.getMaxAttempts()) {
            challenge.setStatus(AuthChallengeStatus.FAILED);
            throw new ValidationException(ErrorCode.CHALLENGE_MAX_ATTEMPTS, challengeId);
        }
        challenge.setAttempts(challenge.getAttempts() + 1);
        if (!verifyCodeHash(code, challenge.getCodeHash())) {
            if (challenge.getAttempts() >= challenge.getMaxAttempts()) {
                challenge.setStatus(AuthChallengeStatus.FAILED);
            }
            throw new ValidationException(ErrorCode.CHALLENGE_INVALID_CODE, challengeId);
        }
        challenge.setStatus(AuthChallengeStatus.VERIFIED);
        return challenge;
    }

    @Override
    @Transactional
    public AuthSession createSession(AppUser user, String authority, Boolean remembered) {
        Long ttl = sessionTtl(authority, remembered);
        return createSession(user, authority, remembered, ttl, false);
    }

    @Transactional
    public AuthSession findSessionByToken(String token) {
        String hash = hashToken(token);
        AuthSession session = authSessionRepository.findByTokenHash(hash).orElse(null);
        if (session == null || session.getRevokedAt() != null) {
            return null;
        }
        if (session.getExpiresAt().isBefore(Instant.now())) {
            session.setRevokedAt(Instant.now());
            return null;
        }
        return session;
    }

    @Override
    @Transactional
    public void activateUser(AppUser user) {
        user.setStatus(UserStatus.ACTIVE);
    }

    @Override
    @Transactional
    public AppUser createStaffUser(String email, String displayName, String tempPassword) {
        AppUser user = authMapper.toStaffUserEntity(
                email, displayName, hashPassword(tempPassword), encrypt(tempPassword));
        return appUserRepository.save(user);
    }

    @Override
    @Transactional
    public AuthSession createSession(AppUser user, String authority, Boolean remembered,
                                      Long ttlSeconds, Boolean activationRequired) {
        String token = generateToken();
        AuthSession session = authMapper.toSessionEntity(
                user, hashToken(token), authority, remembered,
                Instant.now().plusSeconds(ttlSeconds), activationRequired);
        AuthSession saved = authSessionRepository.save(session);
        saved.setPlainToken(token);
        return saved;
    }

    @Override
    @Transactional
    public void activateStaff(AppUser user, String newPassword) {
        user.setPasswordHash(hashPassword(newPassword));
        user.setTempPasswordEncrypted(null);
        user.setMustChangePassword(false);
        user.setActivatedAt(Instant.now());
        user.setStatus(UserStatus.ACTIVE);
    }

    @Override
    @Transactional
    public void resetStaffPassword(AppUser user, String newTempPassword) {
        user.setPasswordHash(hashPassword(newTempPassword));
        user.setTempPasswordEncrypted(encrypt(newTempPassword));
        user.setMustChangePassword(true);
        user.setStatus(UserStatus.PASSWORD_RESET_REQUIRED);
    }

    @Override
    @Transactional
    public void logout(UUID userId) {
        authSessionRepository.revokeAllForUser(userId, Instant.now());
    }

    @Override
    public AppUser findById(UUID id) {
        return appUserRepository.findById(id).orElse(null);
    }

    public AppUser findActiveByEmail(String email) {
        return appUserRepository.findByEmailIgnoreCase(email)
                .filter(u -> u.getStatus() == UserStatus.ACTIVE)
                .orElse(null);
    }

    public AppUser findActiveByPhone(String phone) {
        return appUserRepository.findByPhone(phone)
                .filter(u -> u.getStatus() == UserStatus.ACTIVE)
                .orElse(null);
    }

    public Boolean emailExists(String email) {
        return appUserRepository.existsByEmailIgnoreCase(email);
    }

    public Boolean phoneExists(String phone) {
        return appUserRepository.existsByPhone(phone);
    }

    public String maskEmail(String email) {
        if (email == null) return null;
        int at = email.indexOf('@');
        if (at <= 2) return email;
        return email.charAt(0) + "***" + email.charAt(at - 1) + email.substring(at);
    }

    public String maskPhone(String phone) {
        if (phone == null || phone.length() < 4) return phone;
        return "***" + phone.substring(phone.length() - 4);
    }

    @Override
    public AppUser findByEmail(String email) {
        return appUserRepository.findByEmailIgnoreCase(email).orElse(null);
    }

    @Override
    public Long staffSessionTtl(Boolean remembered) {
        return remembered ? staffRememberedSessionTtlSeconds : staffSessionTtlSeconds;
    }

    @Override
    public Long staffActivationSessionTtl() {
        return staffActivationSessionTtlSeconds;
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

    private void expireUserPendingChallenges(AppUser user) {
        if (user == null) return;
        authChallengeRepository.expirePendingChallenges(
                Instant.now(),
                AuthChallengeStatus.EXPIRED,
                AuthChallengeStatus.PENDING);
    }

    private String hashPassword(String password) {
        return passwordEncoder.encode(password);
    }

    public Boolean verifyPassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
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

package kz.ask.identity.domain;

import jakarta.annotation.PostConstruct;
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
import kz.ask.identity.domain.dto.AppUserDto;
import kz.ask.identity.domain.dto.AuthChallengeDto;
import kz.ask.identity.domain.dto.AuthSessionDto;
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
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class IdentityServiceImpl implements IdentityService {

    private final AppUserRepository appUserRepository;
    private final AuthChallengeRepository authChallengeRepository;
    private final AuthSessionRepository authSessionRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthMapper authMapper;
    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${auth.challenge.ttl}")
    private Integer challengeTtlSeconds;
    @Value("${auth.challenge.code-length}")
    private Integer challengeCodeLength;
    @Value("${auth.challenge.max-attempts}")
    private Integer challengeMaxAttempts;
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
    private SecretKeySpec aesKey;

    @PostConstruct
    private void initAesKey() {
        byte[] keyBytes = sha256Raw(tempPasswordKey);
        this.aesKey = new SecretKeySpec(keyBytes, "AES");
    }

    @Override
    @Transactional
    public AppUserDto createUser(String email, String phone, String displayName, String password, AppRole role) {
        AppUser user = authMapper.toAppUserEntity(
                blankToNull(email), blankToNull(phone), displayName,
                hashPassword(password), role, UserStatus.PENDING);
        AppUser saved = appUserRepository.save(user);
        return authMapper.toAppUserDto(saved);
    }

    @Override
    @Transactional
    public AuthChallengeDto createChallenge(UUID userId, String email, String phone,
                                            AuthChallengeChannel channel,
                                            AuthChallengePurpose purpose,
                                            Boolean rememberMe,
                                            String registrationData) {
        AppUser user = appUserRepository.getReferenceById(userId);
        expireUserPendingChallenges(user);
        String code = generateCode();
        AuthChallenge challenge = authMapper.toChallengeEntity(
                user, email, phone, channel, purpose,
                hashCode(code), challengeMaxAttempts, challengeTtlSeconds,
                rememberMe, registrationData);
        AuthChallenge saved = authChallengeRepository.save(challenge);
        saved.setCodePlain(code);
        return authMapper.toAuthChallengeDto(saved);
    }

    @Override
    @Transactional
    public AuthChallengeDto verifyCode(UUID challengeId, String code) {
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
        return authMapper.toAuthChallengeDto(challenge);
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
        if (session == null || session.getRevokedAt() != null) {
            return null;
        }
        if (session.getExpiresAt().isBefore(Instant.now())) {
            session.setRevokedAt(Instant.now());
            return null;
        }
        return authMapper.toAuthSessionDto(session);
    }

    @Override
    @Transactional
    public void activateUser(UUID userId) {
        AppUser user = appUserRepository.getReferenceById(userId);
        user.setStatus(UserStatus.ACTIVE);
    }

    @Override
    @Transactional
    public AppUserDto createStaffUser(String email, String displayName, String tempPassword) {
        AppUser user = authMapper.toStaffUserEntity(
                email, displayName, hashPassword(tempPassword), encrypt(tempPassword));
        AppUser saved = appUserRepository.save(user);
        return authMapper.toAppUserDto(saved);
    }

    @Override
    @Transactional
    public AuthSessionDto createSession(UUID userId, String authority, Boolean remembered,
                                        Long ttlSeconds, Boolean activationRequired) {
        AppUser user = appUserRepository.getReferenceById(userId);
        String token = generateToken();
        AuthSession session = authMapper.toSessionEntity(
                user, hashToken(token), authority, remembered,
                Instant.now().plusSeconds(ttlSeconds), activationRequired);
        AuthSession saved = authSessionRepository.save(session);
        saved.setPlainToken(token);
        return authMapper.toAuthSessionDto(saved);
    }

    @Override
    @Transactional
    public void activateStaff(UUID userId, String newPassword) {
        AppUser user = appUserRepository.getReferenceById(userId);
        user.setPasswordHash(hashPassword(newPassword));
        user.setTempPasswordEncrypted(null);
        user.setMustChangePassword(false);
        user.setActivatedAt(Instant.now());
        user.setStatus(UserStatus.ACTIVE);
    }

    @Override
    @Transactional
    public void resetStaffPassword(UUID userId, String newTempPassword) {
        AppUser user = appUserRepository.getReferenceById(userId);
        user.setPasswordHash(hashPassword(newTempPassword));
        user.setTempPasswordEncrypted(encrypt(newTempPassword));
        user.setMustChangePassword(true);
        user.setStatus(UserStatus.PASSWORD_RESET_REQUIRED);
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
                .map(authMapper::toAppUserDto)
                .orElse(null);
    }

    @Override
    public AppUserDto findActiveByEmail(String email) {
        return appUserRepository.findByEmailIgnoreCase(email)
                .filter(u -> u.getStatus() == UserStatus.ACTIVE)
                .map(authMapper::toAppUserDto)
                .orElse(null);
    }

    @Override
    public AppUserDto findActiveByPhone(String phone) {
        return appUserRepository.findByPhone(phone)
                .filter(u -> u.getStatus() == UserStatus.ACTIVE)
                .map(authMapper::toAppUserDto)
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
    public AppUserDto findByEmail(String email) {
        return appUserRepository.findByEmailIgnoreCase(email)
                .map(authMapper::toAppUserDto)
                .orElse(null);
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

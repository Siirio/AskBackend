package kz.ask.identity.domain;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.HexFormat;
import java.util.UUID;
import kz.ask.identity.domain.entity.AppUser;
import kz.ask.identity.domain.entity.AuthChallenge;
import kz.ask.identity.domain.entity.AuthSession;
import kz.ask.identity.domain.enums.AppRole;
import kz.ask.identity.domain.enums.AuthChallengeChannel;
import kz.ask.identity.domain.enums.AuthChallengePurpose;
import kz.ask.identity.domain.enums.AuthChallengeStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import kz.ask.identity.domain.enums.UserStatus;
import kz.ask.identity.infrastructure.repository.AppUserRepository;
import kz.ask.identity.infrastructure.repository.AuthChallengeRepository;
import kz.ask.identity.infrastructure.repository.AuthSessionRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class IdentityDomainService {

    private final AppUserRepository appUserRepository;
    private final AuthChallengeRepository authChallengeRepository;
    private final AuthSessionRepository authSessionRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecureRandom secureRandom;

    private final int challengeTtlSeconds;
    private final int challengeCodeLength;
    private final int challengeMaxAttempts;
    private final long customerSessionTtlSeconds;
    private final long customerRememberedSessionTtlSeconds;
    private final long businessSessionTtlSeconds;
    private final long businessRememberedSessionTtlSeconds;

    public IdentityDomainService(
            AppUserRepository appUserRepository,
            AuthChallengeRepository authChallengeRepository,
            AuthSessionRepository authSessionRepository,
            PasswordEncoder passwordEncoder,
            @Value("${auth.challenge.ttl}") int challengeTtlSeconds,
            @Value("${auth.challenge.code-length}") int challengeCodeLength,
            @Value("${auth.challenge.max-attempts}") int challengeMaxAttempts,
            @Value("${auth.customer.session.ttl}") long customerSessionTtlSeconds,
            @Value("${auth.customer.remembered-session.ttl}") long customerRememberedSessionTtlSeconds,
            @Value("${auth.business.session.ttl}") long businessSessionTtlSeconds,
            @Value("${auth.business.remembered-session.ttl}") long businessRememberedSessionTtlSeconds) {
        this.appUserRepository = appUserRepository;
        this.authChallengeRepository = authChallengeRepository;
        this.authSessionRepository = authSessionRepository;
        this.passwordEncoder = passwordEncoder;
        this.secureRandom = new SecureRandom();
        this.challengeTtlSeconds = challengeTtlSeconds;
        this.challengeCodeLength = challengeCodeLength;
        this.challengeMaxAttempts = challengeMaxAttempts;
        this.customerSessionTtlSeconds = customerSessionTtlSeconds;
        this.customerRememberedSessionTtlSeconds = customerRememberedSessionTtlSeconds;
        this.businessSessionTtlSeconds = businessSessionTtlSeconds;
        this.businessRememberedSessionTtlSeconds = businessRememberedSessionTtlSeconds;
    }

    @Transactional
    public AppUser createUser(String email, String phone, String displayName, String password, AppRole role) {
        AppUser user = new AppUser();
        user.setEmail(email);
        user.setPhone(phone);
        user.setDisplayName(displayName);
        user.setPasswordHash(hashPassword(password));
        user.setRole(role);
        user.setStatus(UserStatus.PENDING);
        return appUserRepository.save(user);
    }

    @Transactional
    public AuthChallenge createChallenge(AppUser user, String email, String phone,
                                          AuthChallengeChannel channel,
                                          AuthChallengePurpose purpose,
                                          boolean rememberMe,
                                          String registrationData) {
        expireUserPendingChallenges(user);
        String code = generateCode();
        AuthChallenge challenge = new AuthChallenge();
        challenge.setUser(user);
        challenge.setEmail(email);
        challenge.setPhone(phone);
        challenge.setChannel(channel);
        challenge.setPurpose(purpose);
        challenge.setCodeHash(hashCode(code));
        challenge.setAttempts(0);
        challenge.setMaxAttempts(challengeMaxAttempts);
        challenge.setExpiresAt(Instant.now().plusSeconds(challengeTtlSeconds));
        challenge.setStatus(AuthChallengeStatus.PENDING);
        challenge.setRememberMe(rememberMe);
        challenge.setRegistrationData(registrationData);
        AuthChallenge saved = authChallengeRepository.save(challenge);
        saved.setCodePlain(code);
        return saved;
    }

    @Transactional
    public AuthChallenge verifyCode(UUID challengeId, String code) {
        AuthChallenge challenge = authChallengeRepository.findByIdAndStatus(challengeId, AuthChallengeStatus.PENDING)
                .orElseThrow(() -> new AuthChallengeNotFoundException(challengeId));
        if (challenge.getExpiresAt().isBefore(Instant.now())) {
            challenge.setStatus(AuthChallengeStatus.EXPIRED);
            throw new AuthChallengeExpiredException(challengeId);
        }
        if (challenge.getAttempts() >= challenge.getMaxAttempts()) {
            challenge.setStatus(AuthChallengeStatus.FAILED);
            throw new AuthChallengeMaxAttemptsException(challengeId);
        }
        challenge.setAttempts(challenge.getAttempts() + 1);
        if (!verifyCodeHash(code, challenge.getCodeHash())) {
            if (challenge.getAttempts() >= challenge.getMaxAttempts()) {
                challenge.setStatus(AuthChallengeStatus.FAILED);
            }
            throw new AuthChallengeInvalidCodeException(challengeId);
        }
        challenge.setStatus(AuthChallengeStatus.VERIFIED);
        return challenge;
    }

    @Transactional
    public AuthSession createSession(AppUser user, AppRole role, boolean remembered) {
        String token = generateToken();
        long ttl = sessionTtl(role, remembered);
        AuthSession session = new AuthSession();
        session.setUser(user);
        session.setTokenHash(hashToken(token));
        session.setRole(role);
        session.setRemembered(remembered);
        session.setExpiresAt(Instant.now().plusSeconds(ttl));
        AuthSession saved = authSessionRepository.save(session);
        saved.setPlainToken(token);
        return saved;
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

    @Transactional
    public void activateUser(AppUser user) {
        user.setStatus(UserStatus.ACTIVE);
    }

    @Transactional
    public void logout(UUID userId) {
        authSessionRepository.revokeAllForUser(userId, Instant.now());
    }

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

    public boolean emailExists(String email) {
        return appUserRepository.existsByEmailIgnoreCase(email);
    }

    public boolean phoneExists(String phone) {
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

    private long sessionTtl(AppRole role, boolean remembered) {
        if (role == AppRole.CUSTOMER) {
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

    boolean verifyPassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    static String hashCode(String code) {
        return sha256("code:" + code);
    }

    static String hashToken(String token) {
        return sha256("tok:" + token);
    }

    static boolean verifyCodeHash(String code, String hash) {
        return hashCode(code).equals(hash);
    }

    private static String sha256(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}

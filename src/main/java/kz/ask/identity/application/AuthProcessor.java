package kz.ask.identity.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import kz.ask.audit.domain.SignificantEventService;
import kz.ask.audit.domain.enums.SignificantEventType;
import kz.ask.business.core.domain.BusinessService;
import kz.ask.business.core.domain.dto.BusinessRegistrationResult;
import kz.ask.identity.api.dto.AuthBusinessContextResponse;
import kz.ask.identity.api.dto.VerificationResponse;
import kz.ask.identity.api.dto.AuthSessionResponse;
import kz.ask.identity.api.dto.AuthUserResponse;
import kz.ask.identity.api.dto.BusinessLoginStartRequest;
import kz.ask.identity.api.dto.BusinessRegisterRequest;
import kz.ask.identity.api.dto.CancelVerificationRequest;
import kz.ask.identity.api.dto.ChangePasswordRequest;
import kz.ask.identity.api.dto.CustomerLoginStartRequest;
import kz.ask.identity.api.dto.CustomerRegisterRequest;
import kz.ask.identity.api.dto.LogoutResponse;
import kz.ask.identity.api.dto.RequestEmailChangeRequest;
import kz.ask.identity.api.dto.TwoFactorChangeRequest;
import kz.ask.identity.api.dto.UpdateProfileRequest;
import kz.ask.identity.api.dto.VerifyCodeRequest;
import kz.ask.identity.domain.IdentityService;
import kz.ask.identity.domain.dto.AppUserDto;
import kz.ask.identity.domain.dto.VerificationDto;
import kz.ask.identity.domain.dto.AuthSessionDto;
import kz.ask.identity.authorization.domain.enums.Role;
import kz.ask.identity.domain.enums.VerificationChannel;
import kz.ask.identity.domain.enums.VerificationPurpose;
import kz.ask.identity.domain.enums.UserStatus;
import kz.ask.identity.infrastructure.mail.EmailCodeSender;
import kz.ask.identity.infrastructure.security.AskPrincipal;
import kz.ask.identity.infrastructure.security.JwtTokenService;
import kz.ask.shared.error.AuthException;
import kz.ask.shared.error.ConflictException;
import kz.ask.shared.error.ErrorCode;
import kz.ask.shared.error.InternalServerException;
import kz.ask.shared.error.NotFoundException;
import kz.ask.shared.error.UnauthorizedException;
import kz.ask.shared.error.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class AuthProcessor {

    private final IdentityService identityService;
    private final BusinessService businessService;
    private final EmailCodeSender emailSender;
    private final ObjectMapper objectMapper;
    private final SessionCapabilitiesProcessor sessionCapabilitiesProcessor;
    private final SignificantEventService significantEventService;
    private final JwtTokenService jwtTokenService;

    @Value("${auth.verification.test-mode:false}")
    private Boolean testMode;

    @Value("${auth.challenge.ttl}")
    private Integer challengeTtlSeconds;

    @Transactional
    public VerificationResponse startCustomerLogin(CustomerLoginStartRequest req) {
        AppUserDto user = identityService.findAllActiveByEmail(req.getEmail()).stream()
                .filter(u -> u.getRole() == Role.CUSTOMER)
                .findFirst()
                .orElse(null);
        if (user == null) {
            return unknownLoginChallenge(req.getEmail());
        }
        return createLoginChallenge(user.getId(), req.getEmail(), req.getIsRememberMe());
    }

    @Transactional
    public VerificationResponse registerCustomer(CustomerRegisterRequest req) {
        List<AppUserDto> usersWithEmail = identityService.findAllByEmail(req.getEmail());
        AppUserDto existingUser = usersWithEmail.stream()
                .filter(user -> user.getStatus() == UserStatus.ACTIVE)
                .findFirst()
                .orElse(null);
        if (existingUser != null) {
            throw new ConflictException(ErrorCode.EMAIL_ALREADY_REGISTERED);
        }
        AppUserDto pendingCustomer = usersWithEmail.stream()
                .filter(user -> user.getStatus() == UserStatus.PENDING)
                .filter(user -> user.getRole() == Role.CUSTOMER)
                .findFirst()
                .orElse(null);
        if (pendingCustomer != null) {
            identityService.updatePendingUserCredentials(
                    pendingCustomer.getId(), req.getEmail(), req.getDisplayName(), req.getPassword());
            String registrationData = serializeRegistrationAcceptance(req);
            return createRegisterChallenge(
                    pendingCustomer.getId(), Role.CUSTOMER, req.getEmail(),
                    req.getIsRememberMe(), registrationData);
        }
        if (!usersWithEmail.isEmpty()) {
            return unknownRegistrationChallenge(req.getEmail());
        }
        AppUserDto pendingUser = identityService.createUser(
                req.getEmail(), req.getDisplayName(), req.getPassword(), Role.CUSTOMER);
        String registrationData = serializeRegistrationAcceptance(req);
        return createRegisterChallenge(
                pendingUser.getId(), Role.CUSTOMER, req.getEmail(), req.getIsRememberMe(), registrationData);
    }

    @Transactional
    public VerificationResponse startBusinessLogin(BusinessLoginStartRequest req) {
        AppUserDto user = identityService.findAllActiveByEmail(req.getEmail()).stream()
                .filter(u -> resolveBusinessContext(u) != null)
                .findFirst()
                .orElse(null);
        if (user == null) {
            return unknownLoginChallenge(req.getEmail());
        }
        return createLoginChallenge(user.getId(), req.getEmail(), req.getIsRememberMe());
    }

    @Transactional
    public VerificationResponse registerBusiness(BusinessRegisterRequest req) {
        List<AppUserDto> usersWithEmail = identityService.findAllByEmail(req.getEmail());
        AppUserDto user = usersWithEmail.stream()
                .filter(candidate -> candidate.getStatus() == UserStatus.ACTIVE)
                .findFirst()
                .orElse(null);
        AppUserDto pendingUser = null;
        if (user == null) {
            pendingUser = usersWithEmail.stream()
                    .filter(candidate -> candidate.getStatus() == UserStatus.PENDING)
                    .filter(candidate -> candidate.getRole() == Role.CUSTOMER)
                    .findFirst()
                    .orElse(null);
        }
        if (user == null && pendingUser == null && !usersWithEmail.isEmpty()) {
            return unknownRegistrationChallenge(req.getEmail());
        }
        if (user == null && pendingUser == null) {
            user = identityService.createUser(
                    req.getEmail(), req.getBusinessName(), req.getPassword(), Role.CUSTOMER);
        } else if (pendingUser != null) {
            identityService.updatePendingUserCredentials(
                    pendingUser.getId(), req.getEmail(), req.getBusinessName(), req.getPassword());
            user = pendingUser;
        }
        String registrationData = serializeBusinessRegistration(req);
        return createRegisterChallenge(
                user.getId(), Role.CUSTOMER, req.getEmail(), req.getIsRememberMe(), registrationData);
    }

    @Transactional
    public AuthSessionResponse verifyCode(VerifyCodeRequest req) {
        VerificationDto challenge = identityService.verifyCode(
                req.getVerificationId(),
                req.getCode(),
                null,
                VerificationPurpose.LOGIN,
                VerificationPurpose.REGISTER);
        if (challenge.getPurpose() != VerificationPurpose.LOGIN
                && challenge.getPurpose() != VerificationPurpose.REGISTER) {
            throw new ValidationException(ErrorCode.CHALLENGE_INVALID_CODE, challenge.getId());
        }

        AppUserDto user = identityService.findById(challenge.getUserId());
        boolean isNewRegistration = challenge.getPurpose() == VerificationPurpose.REGISTER;
        if (isNewRegistration) {
            if (user.getStatus() != UserStatus.ACTIVE) {
                identityService.activateUser(user.getId());
            }
            identityService.clearChallengeRegistrationData(challenge.getId());
        }
        identityService.recordLogin(user.getId());
        user = identityService.findById(user.getId());

        BusinessRegistrationResult bizResult = null;
        BusinessRegistrationPayload registrationPayload = deserializeRegistrationPayload(
                challenge.getRegistrationData());
        if (isNewRegistration && registrationPayload != null) {
            if (registrationPayload.getBusinessName() != null) {
                bizResult = createBusiness(user, registrationPayload);
            }
        } else {
            bizResult = resolveBusinessContext(user);
        }

        String authority = authorityForSession(user, bizResult);
        AuthSessionDto session = identityService.createSession(user.getId(), authority, challenge.getIsRememberMe());

        return buildSessionResponse(session, user, bizResult);
    }

    public AuthSessionResponse currentSession(AskPrincipal principal) {
        AppUserDto user = identityService.findById(principal.getUserId());
        if (user == null || user.getStatus() != UserStatus.ACTIVE) {
            throw new UnauthorizedException(ErrorCode.SESSION_INVALID);
        }
        AuthSessionDto session = identityService.findSessionById(principal.getSessionId());
        if (session == null) {
            throw new UnauthorizedException(ErrorCode.SESSION_INVALID);
        }
        BusinessRegistrationResult bizResult = resolveBusinessContext(user);
        AuthSessionResponse resp = buildSessionResponse(null, user, bizResult);
        resp.setAccessToken(jwtTokenService.issue(principal, session.getExpiresAt()));
        resp.setTokenType("Bearer");
        resp.setExpiresAt(session.getExpiresAt());
        resp.setExpiresIn(Math.max(0L, Duration.between(Instant.now(), session.getExpiresAt()).getSeconds()));
        return resp;
    }

    @Transactional
    public AuthSessionResponse updateProfile(AskPrincipal principal, UpdateProfileRequest req) {
        identityService.updateProfile(principal.getUserId(),
                req.getDisplayName(), null, req.getPhone());
        return currentSession(principal);
    }

    @Transactional
    public VerificationResponse requestEmailChange(AskPrincipal principal, RequestEmailChangeRequest req) {
        if (identityService.emailExists(req.getNewEmail())) {
            throw new ConflictException(ErrorCode.EMAIL_ALREADY_REGISTERED);
        }
        AppUserDto user = identityService.findById(principal.getUserId());
        VerificationDto challenge = identityService.createVerification(
                user.getId(), req.getNewEmail(), VerificationChannel.EMAIL,
                VerificationPurpose.EMAIL_CHANGE, false, null);
        if (Boolean.TRUE.equals(testMode)) {
            return buildChallengeResponse(
                    challenge, req.getNewEmail(), user.getRole().name(), challenge.getCodePlain());
        }
        emailSender.sendCode(req.getNewEmail(), challenge.getCodePlain());
        return buildChallengeResponse(
                challenge, identityService.maskEmail(req.getNewEmail()), user.getRole().name(), null);
    }

    @Transactional
    public AuthSessionResponse confirmEmailChange(AskPrincipal principal, VerifyCodeRequest req) {
        VerificationDto challenge = identityService.verifyCode(
                req.getVerificationId(),
                req.getCode(),
                principal.getUserId(),
                VerificationPurpose.EMAIL_CHANGE);
        if (challenge.getPurpose() != VerificationPurpose.EMAIL_CHANGE
                || !principal.getUserId().equals(challenge.getUserId())) {
            throw new ValidationException(ErrorCode.CHALLENGE_INVALID_CODE, challenge.getId());
        }
        if (identityService.emailExists(challenge.getEmail())) {
            throw new ConflictException(ErrorCode.EMAIL_ALREADY_REGISTERED);
        }
        identityService.updateEmail(principal.getUserId(), challenge.getEmail());
        identityService.logout(principal.getUserId());
        significantEventService.record(principal.getUserId(),
                SignificantEventType.EMAIL_CHANGED, null, principal.getUserId(), Map.of());
        AppUserDto user = identityService.findById(principal.getUserId());
        identityService.recordLogin(user.getId());
        AuthSessionDto session = identityService.createSession(user.getId(), "ROLE_USER", false);
        return buildSessionResponse(session, user, resolveBusinessContext(user));
    }

    @Transactional
    public LogoutResponse logout(AskPrincipal principal) {
        identityService.logout(principal.getUserId());
        return LogoutResponse.builder().success(true).build();
    }

    @Transactional
    public VerificationResponse requestPasswordChange(AskPrincipal principal, ChangePasswordRequest req) {
        if (!req.getNewPassword().equals(req.getPasswordConfirmation())) {
            throw new ValidationException(ErrorCode.PASSWORDS_DO_NOT_MATCH);
        }
        AppUserDto user = identityService.findById(principal.getUserId());
        if (!identityService.verifyPassword(req.getCurrentPassword(), user.getPasswordHash())) {
            throw new AuthException(ErrorCode.INVALID_CREDENTIALS);
        }
        VerificationDto challenge = identityService.createVerification(
                user.getId(),
                user.getEmail(),
                VerificationChannel.EMAIL,
                VerificationPurpose.PASSWORD_CHANGE,
                false,
                identityService.encodePassword(req.getNewPassword()));
        if (Boolean.TRUE.equals(testMode)) {
            return buildChallengeResponse(challenge, user.getEmail(), user.getRole().name(), challenge.getCodePlain());
        }
        emailSender.sendCode(user.getEmail(), challenge.getCodePlain());
        return buildChallengeResponse(
                challenge,
                identityService.maskEmail(user.getEmail()),
                user.getRole().name(),
                null);
    }

    @Transactional
    public AuthSessionResponse confirmPasswordChange(AskPrincipal principal, VerifyCodeRequest req) {
        VerificationDto challenge = verifyAuthenticatedChallenge(
                principal,
                req,
                VerificationPurpose.PASSWORD_CHANGE);
        if (challenge.getRegistrationData() == null || challenge.getRegistrationData().isBlank()) {
            throw new ValidationException(ErrorCode.CHALLENGE_INVALID_CODE, challenge.getId());
        }
        identityService.changePasswordHash(principal.getUserId(), challenge.getRegistrationData());
        identityService.revokeOtherSessions(principal.getUserId(), principal.getSessionId());
        identityService.clearChallengeRegistrationData(challenge.getId());
        return currentSession(principal);
    }

    @Transactional
    public VerificationResponse requestTwoFactorChange(AskPrincipal principal, TwoFactorChangeRequest req) {
        AppUserDto user = identityService.findById(principal.getUserId());
        boolean enabled = Boolean.TRUE.equals(req.getEnabled());
        if (Boolean.TRUE.equals(user.getIsTwoFactorEnabled()) == enabled) {
            throw new ValidationException(ErrorCode.TWO_FACTOR_STATE_CHANGED);
        }
        VerificationPurpose purpose = enabled
                ? VerificationPurpose.TWO_FACTOR_ENABLE
                : VerificationPurpose.TWO_FACTOR_DISABLE;
        VerificationDto challenge = identityService.createVerification(
                user.getId(),
                user.getEmail(),
                VerificationChannel.EMAIL,
                purpose,
                false,
                Boolean.toString(enabled));
        if (Boolean.TRUE.equals(testMode)) {
            return buildChallengeResponse(challenge, user.getEmail(), user.getRole().name(), challenge.getCodePlain());
        }
        emailSender.sendCode(user.getEmail(), challenge.getCodePlain());
        return buildChallengeResponse(
                challenge,
                identityService.maskEmail(user.getEmail()),
                user.getRole().name(),
                null);
    }

    @Transactional
    public AuthSessionResponse confirmTwoFactorChange(AskPrincipal principal, VerifyCodeRequest req) {
        VerificationDto challenge = identityService.verifyCode(
                req.getVerificationId(),
                req.getCode(),
                principal.getUserId(),
                VerificationPurpose.TWO_FACTOR_ENABLE,
                VerificationPurpose.TWO_FACTOR_DISABLE);
        if (!"true".equals(challenge.getRegistrationData())
                && !"false".equals(challenge.getRegistrationData())) {
            throw new ValidationException(ErrorCode.CHALLENGE_INVALID_CODE, challenge.getId());
        }
        boolean enabled = Boolean.parseBoolean(challenge.getRegistrationData());
        VerificationPurpose expectedPurpose = enabled
                ? VerificationPurpose.TWO_FACTOR_ENABLE
                : VerificationPurpose.TWO_FACTOR_DISABLE;
        validateAuthenticatedChallenge(principal, challenge, expectedPurpose);
        if (identityService.isTwoFactorEnabled(principal.getUserId()) == enabled) {
            throw new ValidationException(ErrorCode.TWO_FACTOR_STATE_CHANGED);
        }
        identityService.setTwoFactorEnabled(principal.getUserId(), enabled);
        identityService.clearChallengeRegistrationData(challenge.getId());
        return currentSession(principal);
    }

    private VerificationDto verifyAuthenticatedChallenge(
            AskPrincipal principal,
            VerifyCodeRequest req,
            VerificationPurpose purpose) {
        VerificationDto challenge = identityService.verifyCode(
                req.getVerificationId(),
                req.getCode(),
                principal.getUserId(),
                purpose);
        validateAuthenticatedChallenge(principal, challenge, purpose);
        return challenge;
    }

    private void validateAuthenticatedChallenge(
            AskPrincipal principal,
            VerificationDto challenge,
            VerificationPurpose purpose) {
        if (challenge.getPurpose() != purpose
                || !principal.getUserId().equals(challenge.getUserId())) {
            throw new ValidationException(ErrorCode.CHALLENGE_INVALID_CODE, challenge.getId());
        }
    }

    @Transactional
    public void cancelVerification(CancelVerificationRequest req) {
        identityService.cancelVerification(req.getVerificationId());
    }

    private VerificationResponse createLoginChallenge(UUID userId, String email, Boolean rememberMe) {
        VerificationDto challenge = identityService.createVerification(
                userId, email, VerificationChannel.EMAIL, VerificationPurpose.LOGIN, rememberMe, null);
        if (Boolean.TRUE.equals(testMode)) {
            return buildChallengeResponse(challenge, email, "USER", challenge.getCodePlain());
        }
        emailSender.sendCode(email, challenge.getCodePlain());
        String masked = identityService.maskEmail(email);
        return buildChallengeResponse(challenge, masked, "USER", null);
    }

    private VerificationResponse unknownLoginChallenge(String email) {
        return unknownChallenge(email, VerificationPurpose.LOGIN);
    }

    private VerificationResponse unknownRegistrationChallenge(String email) {
        return unknownChallenge(email, VerificationPurpose.REGISTER);
    }

    private VerificationResponse unknownChallenge(
            String email,
            VerificationPurpose purpose) {
        return VerificationResponse.builder()
                .verificationId(UUID.randomUUID())
                .role("USER")
                .purpose(purpose.name())
                .channel(VerificationChannel.EMAIL.name())
                .maskedDestination(identityService.maskEmail(email))
                .expiresAt(Instant.now().plusSeconds(challengeTtlSeconds))
                .build();
    }

    private VerificationResponse createRegisterChallenge(UUID userId, Role role, String email,
                                                           Boolean rememberMe, String registrationData) {
        VerificationDto challenge = identityService.createVerification(
                userId, email, VerificationChannel.EMAIL, VerificationPurpose.REGISTER, rememberMe, registrationData);
        if (Boolean.TRUE.equals(testMode)) {
            return buildChallengeResponse(challenge, email, role.name(), challenge.getCodePlain());
        }
        emailSender.sendCode(email, challenge.getCodePlain());
        String masked = identityService.maskEmail(email);
        return buildChallengeResponse(challenge, masked, role.name(), null);
    }

    private String serializeBusinessRegistration(BusinessRegisterRequest req) {
        BusinessRegistrationPayload payload = new BusinessRegistrationPayload();
        payload.setBusinessName(req.getBusinessName());
        payload.setBranchName(req.getBranchName());
        payload.setBranchCityId(req.getBranchCityId());
        payload.setBranchAddress(req.getBranchAddress());
        payload.setOnlineOnly(req.getOnlineOnly());
        payload.setBusinessCategoryId(req.getBusinessCategoryId());
        payload.setBusinessCategoryName(req.getBusinessCategoryName());
        payload.setBusinessScope(req.getBusinessScope());
        payload.setEmail(req.getEmail());
        payload.setCountryCode(req.getCountryCode());
        payload.setLocale(req.getLocale());
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            throw new InternalServerException(ErrorCode.REGISTRATION_PAYLOAD_ERROR);
        }
    }

    private String serializeRegistrationAcceptance(CustomerRegisterRequest req) {
        BusinessRegistrationPayload payload = new BusinessRegistrationPayload();
        payload.setCountryCode(req.getCountryCode());
        payload.setLocale(req.getLocale());
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            throw new InternalServerException(ErrorCode.REGISTRATION_PAYLOAD_ERROR);
        }
    }

    private BusinessRegistrationPayload deserializeRegistrationPayload(String data) {
        if (data == null) {
            return null;
        }
        try {
            return objectMapper.readValue(data, BusinessRegistrationPayload.class);
        } catch (JsonProcessingException e) {
            throw new InternalServerException(ErrorCode.REGISTRATION_PAYLOAD_ERROR);
        }
    }

    private BusinessRegistrationResult createBusiness(AppUserDto owner, BusinessRegistrationPayload payload) {
        return businessService.registerBusiness(
                owner.getId(),
                payload.getBusinessName(),
                payload.getBusinessCategoryId(),
                payload.getBusinessCategoryName(),
                payload.getBusinessScope(),
                payload.getBranchName(),
                payload.getBranchCityId(),
                payload.getBranchAddress(),
                payload.getOnlineOnly(),
                payload.getEmail(),
                payload.getCountryCode());
    }

    private String authorityForSession(AppUserDto user, BusinessRegistrationResult bizResult) {
        return "ROLE_USER";
    }

    private VerificationResponse buildChallengeResponse(VerificationDto challenge, String maskedDestination, String role, String code) {
        return VerificationResponse.builder()
                .verificationId(challenge.getId())
                .role(role)
                .purpose(challenge.getPurpose().name())
                .channel(challenge.getChannel().name())
                .maskedDestination(maskedDestination)
                .expiresAt(challenge.getExpiresAt())
                .code(code)
                .build();
    }

    private AuthSessionResponse buildSessionResponse(AuthSessionDto session, AppUserDto user, BusinessRegistrationResult bizResult) {
        AuthSessionResponse.AuthSessionResponseBuilder builder = AuthSessionResponse.builder()
                .tokenType("Bearer")
                .isTwoFactorEnabled(Boolean.TRUE.equals(user.getIsTwoFactorEnabled()))
                .user(buildUserResponse(user));

        if (session != null) {
            builder.accessToken(jwtTokenService.issue(
                            new AskPrincipal(user.getId(), session.getId(), user.getDisplayName(), session.getAuthority()),
                            session.getExpiresAt()))
                    .expiresIn(Math.max(0L, Duration.between(Instant.now(), session.getExpiresAt()).getSeconds()))
                    .expiresAt(session.getExpiresAt())
                    .isRemembered(session.getIsRemembered())
                    .isActivationRequired(session.getIsActivationRequired())
                    .role(session.getAuthority())
                    .startRoute(resolveStartRoute());
        } else {
            builder.role(user.getRole().name())
                    .startRoute(resolveStartRoute());
        }

        if (bizResult != null) {
            builder.business(buildBusinessContextResponse(bizResult));
        }
        sessionCapabilitiesProcessor.apply(builder, user);

        return builder.build();
    }

    private AuthUserResponse buildUserResponse(AppUserDto user) {
        return AuthUserResponse.builder()
                .userId(user.getId())
                .displayName(user.getDisplayName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .status(user.getStatus().name())
                .build();
    }

    private AuthBusinessContextResponse buildBusinessContextResponse(BusinessRegistrationResult bizResult) {
        var builder = AuthBusinessContextResponse.builder()
                .businessId(bizResult.getBusiness().getId())
                .businessName(bizResult.getBusiness().getName())
                .businessCategoryId(bizResult.getBusiness().getCategoryId())
                .businessCategoryName(bizResult.getBusiness().getCategoryName())
                .businessScope(bizResult.getBusiness().getScope())
                .membershipId(bizResult.getMember().getId())
                .memberRole(bizResult.getMember().getRole());

        if (bizResult.getBranch() != null) {
            builder.branchId(bizResult.getBranch().getId())
                    .branchName(bizResult.getBranch().getName());
        }

        return builder.build();
    }

    private String resolveStartRoute() {
        return "CLIENT_SEARCH";
    }

    private BusinessRegistrationResult resolveBusinessContext(AppUserDto user) {
        BusinessRegistrationResult bizResult = businessService.findByOwner(user.getId());
        if (bizResult != null) {
            return bizResult;
        }
        return businessService.findByMember(user.getId());
    }

}

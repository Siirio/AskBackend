package kz.ask.identity.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import kz.ask.audit.domain.SignificantEventService;
import kz.ask.audit.domain.enums.SignificantEventType;
import kz.ask.business.domain.BusinessService;
import kz.ask.business.domain.dto.BusinessRegistrationResult;
import kz.ask.identity.api.dto.AuthBusinessContextResponse;
import kz.ask.identity.api.dto.AuthChallengeResponse;
import kz.ask.identity.api.dto.AuthSessionResponse;
import kz.ask.identity.api.dto.AuthUserResponse;
import kz.ask.identity.api.dto.BusinessLoginStartRequest;
import kz.ask.identity.api.dto.BusinessRegisterRequest;
import kz.ask.identity.api.dto.ChangePasswordRequest;
import kz.ask.identity.api.dto.CustomerLoginStartRequest;
import kz.ask.identity.api.dto.CustomerRegisterRequest;
import kz.ask.identity.api.dto.LogoutResponse;
import kz.ask.identity.api.dto.RequestEmailChangeRequest;
import kz.ask.identity.api.dto.UpdateProfileRequest;
import kz.ask.identity.api.dto.VerifyCodeRequest;
import kz.ask.identity.domain.IdentityService;
import kz.ask.identity.domain.dto.AppUserDto;
import kz.ask.identity.domain.dto.AuthChallengeDto;
import kz.ask.identity.domain.dto.AuthSessionDto;
import kz.ask.identity.domain.enums.AppRole;
import kz.ask.identity.domain.enums.AuthChallengeChannel;
import kz.ask.identity.domain.enums.AuthChallengePurpose;
import kz.ask.identity.domain.enums.UserStatus;
import kz.ask.identity.infrastructure.mail.EmailCodeSender;
import kz.ask.identity.infrastructure.security.AskPrincipal;
import kz.ask.legal.domain.LegalService;
import kz.ask.legal.domain.enums.LegalAcceptanceChannel;
import kz.ask.legal.domain.enums.LegalDocumentCode;
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
    private final LegalService legalService;
    private final SignificantEventService significantEventService;

    @Value("${auth.verification.test-mode:false}")
    private Boolean testMode;

    @Value("${auth.challenge.ttl}")
    private Integer challengeTtlSeconds;

    @Transactional
    public AuthChallengeResponse startCustomerLogin(CustomerLoginStartRequest req) {
        AppUserDto user = identityService.findAllActiveByEmail(req.getEmail()).stream()
                .filter(u -> u.getRole() == AppRole.CUSTOMER)
                .findFirst()
                .orElse(null);
        if (user == null) {
            return unknownLoginChallenge(req.getEmail());
        }
        return createLoginChallenge(user.getId(), req.getEmail(), req.getRememberMe());
    }

    @Transactional
    public AuthChallengeResponse registerCustomer(CustomerRegisterRequest req) {
        List<AppUserDto> usersWithEmail = identityService.findAllByEmail(req.getEmail());
        AppUserDto existingUser = usersWithEmail.stream()
                .filter(user -> user.getStatus() == UserStatus.ACTIVE)
                .findFirst()
                .orElse(null);
        if (existingUser != null) {
            String registrationData = serializeRegistrationAcceptance(req);
            return createRegisterChallenge(
                    existingUser.getId(), AppRole.CUSTOMER, req.getEmail(),
                    req.getRememberMe(), registrationData);
        }
        AppUserDto pendingCustomer = usersWithEmail.stream()
                .filter(user -> user.getStatus() == UserStatus.PENDING)
                .filter(user -> user.getRole() == AppRole.CUSTOMER)
                .findFirst()
                .orElse(null);
        if (pendingCustomer != null) {
            String registrationData = serializeRegistrationAcceptance(req);
            return createRegisterChallenge(
                    pendingCustomer.getId(), AppRole.CUSTOMER, req.getEmail(),
                    req.getRememberMe(), registrationData);
        }
        if (!usersWithEmail.isEmpty()) {
            return unknownRegistrationChallenge(req.getEmail());
        }
        AppUserDto pendingUser = identityService.createUser(
                req.getEmail(), req.getDisplayName(), req.getPassword(), AppRole.CUSTOMER);
        String registrationData = serializeRegistrationAcceptance(req);
        return createRegisterChallenge(
                pendingUser.getId(), AppRole.CUSTOMER, req.getEmail(), req.getRememberMe(), registrationData);
    }

    @Transactional
    public AuthChallengeResponse startBusinessLogin(BusinessLoginStartRequest req) {
        AppUserDto user = identityService.findAllActiveByEmail(req.getEmail()).stream()
                .filter(u -> resolveBusinessContext(u) != null)
                .findFirst()
                .orElse(null);
        if (user == null) {
            return unknownLoginChallenge(req.getEmail());
        }
        return createLoginChallenge(user.getId(), req.getEmail(), req.getRememberMe());
    }

    @Transactional
    public AuthChallengeResponse registerBusiness(BusinessRegisterRequest req) {
        List<AppUserDto> usersWithEmail = identityService.findAllByEmail(req.getEmail());
        AppUserDto user = usersWithEmail.stream()
                .filter(candidate -> candidate.getStatus() == UserStatus.ACTIVE)
                .findFirst()
                .orElse(null);
        if (user == null) {
            user = usersWithEmail.stream()
                    .filter(candidate -> candidate.getStatus() == UserStatus.PENDING)
                    .filter(candidate -> candidate.getRole() == AppRole.CUSTOMER)
                    .findFirst()
                    .orElse(null);
        }
        if (user == null && !usersWithEmail.isEmpty()) {
            return unknownRegistrationChallenge(req.getEmail());
        }
        if (user == null) {
            user = identityService.createUser(
                    req.getEmail(), req.getBusinessName(), req.getPassword(), AppRole.CUSTOMER);
        }
        String registrationData = serializeBusinessRegistration(req);
        return createRegisterChallenge(
                user.getId(), AppRole.CUSTOMER, req.getEmail(), req.getRememberMe(), registrationData);
    }

    @Transactional
    public AuthSessionResponse verifyCode(VerifyCodeRequest req) {
        AuthChallengeDto challenge = identityService.verifyCode(req.getAuthChallengeId(), req.getCode());
        if (challenge.getPurpose() == AuthChallengePurpose.EMAIL_CHANGE) {
            throw new ValidationException(ErrorCode.CHALLENGE_INVALID_CODE, challenge.getId());
        }

        AppUserDto user = identityService.findById(challenge.getUserId());
        boolean isNewRegistration = challenge.getPurpose() == AuthChallengePurpose.REGISTER;
        if (isNewRegistration) {
            if (user.getStatus() != UserStatus.ACTIVE) {
                identityService.activateUser(user.getId());
            }
            identityService.clearChallengeRegistrationData(challenge.getId());
        }
        user = canonicalUser(user);
        identityService.recordLogin(user.getId());
        user = identityService.findById(user.getId());

        List<String> allRoles = identityService.findAllByEmail(user.getEmail()).stream()
                .filter(u -> u.getStatus() == UserStatus.ACTIVE)
                .map(u -> u.getRole().name())
                .distinct()
                .toList();

        BusinessRegistrationResult bizResult = null;
        BusinessRegistrationPayload registrationPayload = deserializeRegistrationPayload(
                challenge.getRegistrationData());
        if (isNewRegistration && registrationPayload != null) {
            legalService.acceptActiveDocuments(
                    user.getId(),
                    registrationPayload.getAcceptedDocumentCodes(),
                    registrationPayload.getCountryCode(),
                    registrationPayload.getLocale(),
                    LegalAcceptanceChannel.WEB_REGISTRATION);
            if (registrationPayload.getBusinessName() != null) {
                bizResult = createBusiness(user, registrationPayload);
            }
        } else {
            bizResult = resolveBusinessContext(user);
        }

        String authority = authorityForSession(user, bizResult);
        AuthSessionDto session = identityService.createSession(user.getId(), authority, challenge.getRememberMe());

        AuthSessionResponse response = buildSessionResponse(session, user, bizResult);
        response.setAllRoles(allRoles);
        return response;
    }

    public AuthSessionResponse currentSession(AskPrincipal principal) {
        AppUserDto user = identityService.findById(principal.getUserId());
        if (user == null || user.getStatus() != UserStatus.ACTIVE) {
            throw new UnauthorizedException(ErrorCode.SESSION_INVALID);
        }
        List<String> allRoles = identityService.findAllByEmail(user.getEmail()).stream()
                .filter(u -> u.getStatus() == UserStatus.ACTIVE)
                .map(u -> u.getRole().name())
                .distinct()
                .toList();

        BusinessRegistrationResult bizResult = resolveBusinessContext(user);
        AuthSessionResponse resp = buildSessionResponse(null, user, bizResult);
        resp.setAccessToken(null);
        resp.setAllRoles(allRoles);
        return resp;
    }

    @Transactional
    public AuthSessionResponse updateProfile(AskPrincipal principal, UpdateProfileRequest req) {
        identityService.updateProfile(principal.getUserId(),
                req.getDisplayName(), null);
        return currentSession(principal);
    }

    @Transactional
    public AuthChallengeResponse requestEmailChange(AskPrincipal principal, RequestEmailChangeRequest req) {
        if (identityService.emailExists(req.getNewEmail())) {
            throw new ConflictException(ErrorCode.EMAIL_ALREADY_REGISTERED);
        }
        AppUserDto user = identityService.findById(principal.getUserId());
        AuthChallengeDto challenge = identityService.createChallenge(
                user.getId(), req.getNewEmail(), AuthChallengeChannel.EMAIL,
                AuthChallengePurpose.EMAIL_CHANGE, false, null);
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
        AuthChallengeDto challenge = identityService.verifyCode(req.getAuthChallengeId(), req.getCode());
        if (challenge.getPurpose() != AuthChallengePurpose.EMAIL_CHANGE
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
    public AuthSessionResponse changePassword(AskPrincipal principal, ChangePasswordRequest req) {
        AppUserDto user = identityService.findById(principal.getUserId());
        if (!identityService.verifyPassword(req.getCurrentPassword(), user.getPasswordHash())) {
            throw new AuthException(ErrorCode.INVALID_CREDENTIALS);
        }

        identityService.changePassword(user.getId(), req.getNewPassword());
        identityService.logout(user.getId());
        identityService.recordLogin(user.getId());

        BusinessRegistrationResult bizResult = resolveBusinessContext(user);

        String authority = authorityForSession(user, bizResult);
        AuthSessionDto session = identityService.createSession(user.getId(), authority, false);
        AuthSessionResponse response = buildSessionResponse(session, user, bizResult);
        response.setAllRoles(identityService.findAllByEmail(user.getEmail()).stream()
                .filter(u -> u.getStatus() == UserStatus.ACTIVE)
                .map(u -> u.getRole().name())
                .distinct()
                .toList());
        return response;
    }

    @Transactional
    public AuthSessionResponse toggleTwoFactor(AskPrincipal principal) {
        identityService.toggleTwoFactor(principal.getUserId());
        return currentSession(principal);
    }

    private AuthChallengeResponse createLoginChallenge(UUID userId, String email, Boolean rememberMe) {
        AuthChallengeDto challenge = identityService.createChallenge(
                userId, email, AuthChallengeChannel.EMAIL, AuthChallengePurpose.LOGIN, rememberMe, null);
        if (Boolean.TRUE.equals(testMode)) {
            return buildChallengeResponse(challenge, email, "USER", challenge.getCodePlain());
        }
        emailSender.sendCode(email, challenge.getCodePlain());
        String masked = identityService.maskEmail(email);
        return buildChallengeResponse(challenge, masked, "USER", null);
    }

    private AuthChallengeResponse unknownLoginChallenge(String email) {
        return unknownChallenge(email, AuthChallengePurpose.LOGIN);
    }

    private AuthChallengeResponse unknownRegistrationChallenge(String email) {
        return unknownChallenge(email, AuthChallengePurpose.REGISTER);
    }

    private AuthChallengeResponse unknownChallenge(
            String email,
            AuthChallengePurpose purpose) {
        return AuthChallengeResponse.builder()
                .authChallengeId(UUID.randomUUID())
                .role("USER")
                .purpose(purpose.name())
                .channel(AuthChallengeChannel.EMAIL.name())
                .maskedDestination(identityService.maskEmail(email))
                .expiresAt(Instant.now().plusSeconds(challengeTtlSeconds))
                .build();
    }

    private AuthChallengeResponse createRegisterChallenge(UUID userId, AppRole role, String email,
                                                           Boolean rememberMe, String registrationData) {
        AuthChallengeDto challenge = identityService.createChallenge(
                userId, email, AuthChallengeChannel.EMAIL, AuthChallengePurpose.REGISTER, rememberMe, registrationData);
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
        payload.setEmail(req.getEmail());
        payload.setCountryCode(req.getCountryCode());
        payload.setLocale(req.getLocale());
        payload.setAcceptedDocumentCodes(Set.of(
                LegalDocumentCode.USER_TERMS,
                LegalDocumentCode.PRIVACY_POLICY,
                LegalDocumentCode.SELLER_TERMS));
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
        payload.setAcceptedDocumentCodes(Set.of(
                LegalDocumentCode.USER_TERMS,
                LegalDocumentCode.PRIVACY_POLICY));
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
                payload.getBranchName(),
                payload.getBranchCityId(),
                payload.getBranchAddress(),
                payload.getOnlineOnly(),
                payload.getEmail());
    }

    private String authorityForSession(AppUserDto user, BusinessRegistrationResult bizResult) {
        return "ROLE_USER";
    }

    private AuthChallengeResponse buildChallengeResponse(AuthChallengeDto challenge, String maskedDestination, String role, String code) {
        return AuthChallengeResponse.builder()
                .authChallengeId(challenge.getId())
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
                .user(buildUserResponse(user));

        if (session != null) {
            builder.accessToken(session.getPlainToken())
                    .expiresAt(session.getExpiresAt())
                    .remembered(session.getRemembered())
                    .activationRequired(session.getActivationRequired())
                    .role(session.getAuthority())
                    .startRoute(resolveStartRoute(session.getAuthority(), bizResult, user));
        } else {
            builder.role(user.getRole().name())
                    .startRoute(resolveStartRoute(null, bizResult, user));
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
                .status(user.getStatus().name())
                .build();
    }

    private AuthBusinessContextResponse buildBusinessContextResponse(BusinessRegistrationResult bizResult) {
        var builder = AuthBusinessContextResponse.builder()
                .businessId(bizResult.getBusiness().getId())
                .businessName(bizResult.getBusiness().getName())
                .membershipId(bizResult.getMember().getId())
                .memberRole(bizResult.getMember().getRole());

        if (bizResult.getBranch() != null) {
            builder.branchId(bizResult.getBranch().getId())
                    .branchName(bizResult.getBranch().getName());
        }

        return builder.build();
    }

    private String resolveStartRoute(String authority, BusinessRegistrationResult bizResult, AppUserDto user) {
        if (bizResult != null) {
            return "BUSINESS_CABINET";
        }
        return "CLIENT_SEARCH";
    }

    private BusinessRegistrationResult resolveBusinessContext(AppUserDto user) {
        BusinessRegistrationResult bizResult = businessService.findByOwner(user.getId());
        if (bizResult != null) {
            return bizResult;
        }
        return businessService.findByMember(user.getId());
    }

    private AppUserDto canonicalUser(AppUserDto user) {
        return identityService.findAllActiveByEmail(user.getEmail()).stream()
                .filter(candidate -> candidate.getRole() == AppRole.CUSTOMER)
                .findFirst()
                .orElse(user);
    }
}

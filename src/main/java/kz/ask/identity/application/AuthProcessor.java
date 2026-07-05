package kz.ask.identity.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import kz.ask.business.domain.BusinessService;
import kz.ask.business.domain.BranchMemberService;
import java.util.UUID;
import kz.ask.business.domain.dto.BusinessRegistrationResult;
import kz.ask.identity.api.dto.AuthBusinessContextResponse;
import kz.ask.identity.api.dto.AuthChallengeResponse;
import kz.ask.identity.api.dto.AuthSessionResponse;
import kz.ask.identity.api.dto.AuthUserResponse;
import kz.ask.identity.api.dto.BusinessLoginStartRequest;
import kz.ask.identity.api.dto.BusinessRegisterRequest;
import kz.ask.identity.api.dto.CustomerLoginStartRequest;
import kz.ask.identity.api.dto.CustomerRegisterRequest;
import kz.ask.identity.api.dto.LogoutResponse;
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
import kz.ask.identity.infrastructure.sms.SmsCodeSender;
import kz.ask.shared.error.ConflictException;
import kz.ask.shared.error.ErrorCode;
import kz.ask.shared.error.ForbiddenException;
import kz.ask.shared.error.InternalServerException;
import kz.ask.shared.error.NotFoundException;
import kz.ask.shared.error.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class AuthProcessor {

    private final IdentityService identityService;
    private final BusinessService businessService;
    private final BranchMemberService branchMemberService;
    private final EmailCodeSender emailSender;
    private final SmsCodeSender smsSender;
    private final ObjectMapper objectMapper;

    @Value("${auth.verification.test-mode:false}")
    private Boolean testMode;

    @Transactional
    public AuthChallengeResponse startCustomerLogin(CustomerLoginStartRequest req) {
        AppUserDto user = findUserForLogin(req.getEmail(), req.getPhone());
        if (user == null) {
            throw new NotFoundException(ErrorCode.USER_NOT_FOUND);
        }
        if (user.getRole() != AppRole.CUSTOMER) {
            throw new ForbiddenException(ErrorCode.ROLE_MISMATCH);
        }
        return createLoginChallenge(user.getId(), user.getRole(), req.getEmail(), req.getPhone(), req.getRememberMe());
    }

    @Transactional
    public AuthChallengeResponse registerCustomer(CustomerRegisterRequest req) {
        if (req.getEmail() != null && !req.getEmail().isBlank() && identityService.emailExists(req.getEmail())) {
            throw new ConflictException(ErrorCode.EMAIL_ALREADY_REGISTERED);
        }
        if (req.getPhone() != null && !req.getPhone().isBlank() && identityService.phoneExists(req.getPhone())) {
            throw new ConflictException(ErrorCode.PHONE_ALREADY_REGISTERED);
        }
        String registrationData = serializeUserRegistration(
                blankToNull(req.getEmail()), blankToNull(req.getPhone()),
                req.getDisplayName(), req.getPassword(), "CUSTOMER");
        return createRegisterChallenge(null, AppRole.CUSTOMER, req.getEmail(), req.getPhone(), req.getRememberMe(), registrationData);
    }

    @Transactional
    public AuthChallengeResponse startBusinessLogin(BusinessLoginStartRequest req) {
        AppUserDto user = findUserForLogin(req.getEmail(), req.getPhone());
        if (user == null) {
            throw new NotFoundException(ErrorCode.USER_NOT_FOUND);
        }
        if (user.getRole() != AppRole.BUSINESS) {
            throw new ForbiddenException(ErrorCode.ROLE_MISMATCH);
        }
        return createLoginChallenge(user.getId(), user.getRole(), req.getEmail(), req.getPhone(), req.getRememberMe());
    }

    @Transactional
    public AuthChallengeResponse registerBusiness(BusinessRegisterRequest req) {
        if (req.getEmail() != null && !req.getEmail().isBlank() && identityService.emailExists(req.getEmail())) {
            throw new ConflictException(ErrorCode.EMAIL_ALREADY_REGISTERED);
        }
        if (req.getPhone() != null && !req.getPhone().isBlank() && identityService.phoneExists(req.getPhone())) {
            throw new ConflictException(ErrorCode.PHONE_ALREADY_REGISTERED);
        }
        String email = blankToNull(req.getEmail());
        String phone = blankToNull(req.getPhone());
        String registrationData = serializeBusinessRegistration(email, phone,
                req.getBusinessName(), req.getPassword(), req);
        return createRegisterChallenge(null, AppRole.BUSINESS, email, phone, req.getRememberMe(), registrationData);
    }

    @Transactional
    public AuthSessionResponse verifyCode(VerifyCodeRequest req) {
        AuthChallengeDto challenge = identityService.verifyCode(req.getAuthChallengeId(), req.getCode());

        AppUserDto user;
        if (challenge.getPurpose() == AuthChallengePurpose.REGISTER) {
            user = createUserFromStoredRegistration(challenge.getRegistrationData());
            identityService.activateUser(user.getId());
        } else {
            user = identityService.findById(challenge.getUserId());
        }

        BusinessRegistrationResult bizResult = null;
        if (challenge.getPurpose() == AuthChallengePurpose.REGISTER
                && user.getRole() == AppRole.BUSINESS
                && challenge.getRegistrationData() != null) {
            bizResult = deserializeAndCreateBusiness(user, challenge.getRegistrationData());
        }

        String authority = authorityForSession(user, bizResult);
        AuthSessionDto session = identityService.createSession(user.getId(), authority, challenge.getRememberMe());

        return buildSessionResponse(session, user, bizResult);
    }

    public AuthSessionResponse currentSession(AskPrincipal principal) {
        AppUserDto user = identityService.findById(principal.getUserId());
        if (user == null || user.getStatus() != UserStatus.ACTIVE) {
            throw new UnauthorizedException(ErrorCode.SESSION_INVALID);
        }
        BusinessRegistrationResult bizResult = null;
        if (user.getRole() == AppRole.BUSINESS) {
            bizResult = businessService.findByOwner(user.getId());
        }
        AuthSessionResponse resp = buildSessionResponse(null, user, bizResult);
        resp.setAccessToken(null);
        return resp;
    }

    @Transactional
    public AuthSessionResponse updateProfile(AskPrincipal principal, UpdateProfileRequest req) {
        identityService.updateProfile(principal.getUserId(),
                req.getDisplayName(), req.getEmail(), req.getPhone());
        return currentSession(principal);
    }

    @Transactional
    public LogoutResponse logout(AskPrincipal principal) {
        identityService.logout(principal.getUserId());
        return LogoutResponse.builder().success(true).build();
    }

    private AuthChallengeResponse createLoginChallenge(UUID userId, AppRole role, String email, String phone, Boolean rememberMe) {
        AuthChallengeChannel channel = email != null && !email.isBlank()
                ? AuthChallengeChannel.EMAIL : AuthChallengeChannel.SMS;
        String destination = email != null && !email.isBlank() ? email : phone;
        AuthChallengeDto challenge = identityService.createChallenge(
                userId, email, phone, channel, AuthChallengePurpose.LOGIN, rememberMe, null);
        if (Boolean.TRUE.equals(testMode)) {
            return buildChallengeResponse(challenge, destination, role.name(), challenge.getCodePlain());
        }
        sendCode(channel, destination, challenge.getCodePlain());
        String masked = channel == AuthChallengeChannel.EMAIL
                ? identityService.maskEmail(destination) : identityService.maskPhone(destination);
        return buildChallengeResponse(challenge, masked, role.name(), null);
    }

    private AuthChallengeResponse createRegisterChallenge(UUID userId, AppRole role, String email, String phone,
                                                           Boolean rememberMe, String registrationData) {
        AuthChallengeChannel channel = email != null && !email.isBlank()
                ? AuthChallengeChannel.EMAIL : AuthChallengeChannel.SMS;
        String destination = email != null && !email.isBlank() ? email : phone;
        AuthChallengeDto challenge = identityService.createChallenge(
                userId, email, phone, channel, AuthChallengePurpose.REGISTER, rememberMe, registrationData);
        if (Boolean.TRUE.equals(testMode)) {
            return buildChallengeResponse(challenge, destination, role.name(), challenge.getCodePlain());
        }
        sendCode(channel, destination, challenge.getCodePlain());
        String masked = channel == AuthChallengeChannel.EMAIL
                ? identityService.maskEmail(destination) : identityService.maskPhone(destination);
        return buildChallengeResponse(challenge, masked, role.name(), null);
    }

    private void sendCode(AuthChallengeChannel channel, String destination, String code) {
        if (channel == AuthChallengeChannel.EMAIL) {
            emailSender.sendCode(destination, code);
        } else {
            smsSender.sendCode(destination, code);
        }
    }

    private AppUserDto findUserForLogin(String email, String phone) {
        if (email != null && !email.isBlank()) {
            return identityService.findActiveByEmail(email);
        }
        if (phone != null && !phone.isBlank()) {
            return identityService.findActiveByPhone(phone);
        }
        return null;
    }

    private String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s;
    }

    private String serializeUserRegistration(String email, String phone, String displayName,
                                             String password, String role) {
        BusinessRegistrationPayload payload = new BusinessRegistrationPayload();
        payload.setEmail(email);
        payload.setPhone(phone);
        payload.setDisplayName(displayName);
        payload.setPassword(password);
        payload.setRole(role);
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            throw new InternalServerException(ErrorCode.REGISTRATION_PAYLOAD_ERROR);
        }
    }

    private String serializeBusinessRegistration(String email, String phone, String displayName,
                                                  String password, BusinessRegisterRequest req) {
        BusinessRegistrationPayload payload = new BusinessRegistrationPayload();
        payload.setEmail(email);
        payload.setPhone(phone);
        payload.setDisplayName(displayName);
        payload.setPassword(password);
        payload.setRole("BUSINESS");
        payload.setBusinessName(req.getBusinessName());
        payload.setBranchName(req.getBranchName());
        payload.setBranchCityId(req.getBranchCityId());
        payload.setBranchAddress(req.getBranchAddress());
        payload.setOnlineOnly(req.getOnlineOnly());
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            throw new InternalServerException(ErrorCode.REGISTRATION_PAYLOAD_ERROR);
        }
    }

    private AppUserDto createUserFromStoredRegistration(String registrationData) {
        try {
            BusinessRegistrationPayload payload = objectMapper.readValue(registrationData, BusinessRegistrationPayload.class);
            AppRole role = "BUSINESS".equals(payload.getRole()) ? AppRole.BUSINESS : AppRole.CUSTOMER;
            return identityService.createUser(
                    payload.getEmail(), payload.getPhone(), payload.getDisplayName(),
                    payload.getPassword(), role);
        } catch (JsonProcessingException e) {
            throw new InternalServerException(ErrorCode.REGISTRATION_PAYLOAD_ERROR);
        }
    }

    private BusinessRegistrationResult deserializeAndCreateBusiness(AppUserDto owner, String data) {
        try {
            BusinessRegistrationPayload payload = objectMapper.readValue(data, BusinessRegistrationPayload.class);
            return businessService.registerBusiness(
                    owner.getId(),
                    payload.getBusinessName(),
                    payload.getBranchName(),
                    payload.getBranchCityId(),
                    payload.getBranchAddress(),
                    payload.getOnlineOnly(),
                    payload.getEmail(),
                    payload.getPhone());
        } catch (JsonProcessingException e) {
            throw new InternalServerException(ErrorCode.REGISTRATION_PAYLOAD_ERROR);
        }
    }

    private String authorityForSession(AppUserDto user, BusinessRegistrationResult bizResult) {
        if (user.getRole() == AppRole.CUSTOMER) {
            return "ROLE_CUSTOMER";
        }
        if (bizResult != null && bizResult.getMember() != null) {
            return "ROLE_BUSINESS_" + bizResult.getMember().getRole();
        }
        if (branchMemberService.isBranchStaff(user.getId())) {
            return "ROLE_BUSINESS_STAFF";
        }
        return "ROLE_BUSINESS_OWNER";
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
                    .startRoute(resolveStartRoute(session.getAuthority(), null, user));
        } else {
            builder.role(user.getRole().name())
                    .startRoute(resolveStartRoute(null, bizResult, user));
        }

        if (bizResult != null) {
            builder.business(buildBusinessContextResponse(bizResult));
        }

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
        return AuthBusinessContextResponse.builder()
                .businessId(bizResult.getBusiness().getId())
                .businessName(bizResult.getBusiness().getName())
                .branchId(bizResult.getBranch().getId())
                .branchName(bizResult.getBranch().getName())
                .membershipId(bizResult.getMember().getId())
                .memberRole(bizResult.getMember().getRole())
                .build();
    }

    private String resolveStartRoute(String authority, BusinessRegistrationResult bizResult, AppUserDto user) {
        if (user.getRole() == AppRole.CUSTOMER) {
            return "CLIENT_SEARCH";
        }
        if (authority != null) {
            if (authority.contains("OWNER")) {
                return "OWNER_BRANCHES";
            }
            if (authority.contains("STAFF")) {
                return "BRANCH_WORKSPACE";
            }
        }
        if (bizResult != null) {
            return "OWNER_BRANCHES";
        }
        return "BRANCH_WORKSPACE";
    }
}

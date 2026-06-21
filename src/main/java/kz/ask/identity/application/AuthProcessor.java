package kz.ask.identity.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import kz.ask.business.domain.BusinessService;
import kz.ask.business.domain.BusinessService.BusinessRegistrationResult;
import kz.ask.identity.api.dto.AuthChallengeResponse;
import kz.ask.identity.api.dto.AuthSessionResponse;
import kz.ask.identity.api.dto.BusinessLoginStartRequest;
import kz.ask.identity.api.dto.BusinessRegisterRequest;
import kz.ask.identity.api.dto.CustomerLoginStartRequest;
import kz.ask.identity.api.dto.CustomerRegisterRequest;
import kz.ask.identity.api.dto.LogoutResponse;
import kz.ask.identity.api.dto.VerifyCodeRequest;
import kz.ask.identity.domain.IdentityService;
import kz.ask.identity.domain.entity.AppUser;
import kz.ask.identity.domain.entity.AuthChallenge;
import kz.ask.identity.domain.entity.AuthSession;
import kz.ask.identity.domain.enums.AppRole;
import kz.ask.identity.domain.enums.AuthChallengeChannel;
import kz.ask.identity.domain.enums.AuthChallengePurpose;
import kz.ask.identity.domain.enums.UserStatus;
import kz.ask.identity.infrastructure.mail.EmailCodeSender;
import kz.ask.identity.infrastructure.mapper.AuthMapper;
import kz.ask.identity.infrastructure.security.AskPrincipal;
import kz.ask.identity.infrastructure.sms.SmsCodeSender;
import kz.ask.shared.error.ConflictException;
import kz.ask.shared.error.ErrorCode;
import kz.ask.shared.error.ForbiddenException;
import kz.ask.shared.error.InternalServerException;
import kz.ask.shared.error.NotFoundException;
import kz.ask.shared.error.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class AuthProcessor {

    private final IdentityService identityService;
    private final BusinessService businessService;
    private final EmailCodeSender emailSender;
    private final SmsCodeSender smsSender;
    private final ObjectMapper objectMapper;
    private final AuthMapper authMapper;

    @Transactional
    public AuthChallengeResponse startCustomerLogin(CustomerLoginStartRequest req) {
        AppUser user = findUserForLogin(req.getEmail(), req.getPhone());
        if (user == null) {
            throw new NotFoundException(ErrorCode.USER_NOT_FOUND);
        }
        if (user.getRole() != AppRole.CUSTOMER) {
            throw new ForbiddenException(ErrorCode.ROLE_MISMATCH);
        }
        return createLoginChallenge(user, req.getEmail(), req.getPhone(), req.getRememberMe());
    }

    @Transactional
    public AuthChallengeResponse registerCustomer(CustomerRegisterRequest req) {
        if (req.getEmail() != null && !req.getEmail().isBlank() && identityService.emailExists(req.getEmail())) {
            throw new ConflictException(ErrorCode.EMAIL_ALREADY_REGISTERED);
        }
        if (req.getPhone() != null && !req.getPhone().isBlank() && identityService.phoneExists(req.getPhone())) {
            throw new ConflictException(ErrorCode.PHONE_ALREADY_REGISTERED);
        }
        AppUser user = identityService.createUser(
                blankToNull(req.getEmail()),
                blankToNull(req.getPhone()),
                req.getDisplayName(),
                req.getPassword(),
                AppRole.CUSTOMER);
        return createRegisterChallenge(user, req.getEmail(), req.getPhone(), req.getRememberMe(), null);
    }

    @Transactional
    public AuthChallengeResponse startBusinessLogin(BusinessLoginStartRequest req) {
        AppUser user = findUserForLogin(req.getEmail(), req.getPhone());
        if (user == null) {
            throw new NotFoundException(ErrorCode.USER_NOT_FOUND);
        }
        if (user.getRole() != AppRole.BUSINESS) {
            throw new ForbiddenException(ErrorCode.ROLE_MISMATCH);
        }
        return createLoginChallenge(user, req.getEmail(), req.getPhone(), req.getRememberMe());
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
        AppUser user = identityService.createUser(email, phone, req.getBusinessName(), req.getPassword(), AppRole.BUSINESS);
        String registrationData = serializeBusinessRegistration(req);
        return createRegisterChallenge(user, email, phone, req.getRememberMe(), registrationData);
    }

    @Transactional
    public AuthSessionResponse verifyCode(VerifyCodeRequest req) {
        AuthChallenge challenge = identityService.verifyCode(req.getAuthChallengeId(), req.getCode());
        AppUser user = challenge.getUser();

        BusinessRegistrationResult bizResult = null;
        if (challenge.getPurpose() == AuthChallengePurpose.REGISTER) {
            identityService.activateUser(user);
            if (user.getRole() == AppRole.BUSINESS && challenge.getRegistrationData() != null) {
                bizResult = deserializeAndCreateBusiness(user, challenge.getRegistrationData());
            }
        }

        String authority = authorityForSession(user, bizResult);
        AuthSession session = identityService.createSession(user, authority, challenge.getRememberMe());
        return authMapper.toSessionResponse(session, user, bizResult);
    }

    public AuthSessionResponse currentSession(AskPrincipal principal) {
        AppUser user = identityService.findById(principal.getUserId());
        if (user == null || user.getStatus() != UserStatus.ACTIVE) {
            throw new UnauthorizedException(ErrorCode.SESSION_INVALID);
        }
        BusinessRegistrationResult bizResult = null;
        if (user.getRole() == AppRole.BUSINESS) {
            bizResult = businessService.findByOwner(user.getId());
        }
        AuthSessionResponse resp = authMapper.toSessionResponse(null, user, bizResult);
        resp.setAccessToken(null);
        return resp;
    }

    @Transactional
    public LogoutResponse logout(AskPrincipal principal) {
        identityService.logout(principal.getUserId());
        return LogoutResponse.builder().success(true).build();
    }

    private AuthChallengeResponse createLoginChallenge(AppUser user, String email, String phone, Boolean rememberMe) {
        AuthChallengeChannel channel = email != null && !email.isBlank()
                ? AuthChallengeChannel.EMAIL : AuthChallengeChannel.SMS;
        String destination = email != null && !email.isBlank() ? email : phone;
        AuthChallenge challenge = identityService.createChallenge(
                user, email, phone, channel, AuthChallengePurpose.LOGIN, rememberMe, null);
        sendCode(channel, destination, challenge.getCodePlain());
        String masked = channel == AuthChallengeChannel.EMAIL
                ? identityService.maskEmail(destination) : identityService.maskPhone(destination);
        return authMapper.toChallengeResponse(challenge, masked, user.getRole().name());
    }

    private AuthChallengeResponse createRegisterChallenge(AppUser user, String email, String phone,
                                                           Boolean rememberMe, String registrationData) {
        AuthChallengeChannel channel = email != null && !email.isBlank()
                ? AuthChallengeChannel.EMAIL : AuthChallengeChannel.SMS;
        String destination = email != null && !email.isBlank() ? email : phone;
        AuthChallenge challenge = identityService.createChallenge(
                user, email, phone, channel, AuthChallengePurpose.REGISTER, rememberMe, registrationData);
        sendCode(channel, destination, challenge.getCodePlain());
        String masked = channel == AuthChallengeChannel.EMAIL
                ? identityService.maskEmail(destination) : identityService.maskPhone(destination);
        return authMapper.toChallengeResponse(challenge, masked, user.getRole().name());
    }

    private void sendCode(AuthChallengeChannel channel, String destination, String code) {
        if (channel == AuthChallengeChannel.EMAIL) {
            emailSender.sendCode(destination, code);
        } else {
            smsSender.sendCode(destination, code);
        }
    }

    private AppUser findUserForLogin(String email, String phone) {
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

    private String serializeBusinessRegistration(BusinessRegisterRequest req) {
        BusinessRegistrationPayload payload = new BusinessRegistrationPayload();
        payload.setBusinessName(req.getBusinessName());
        payload.setBranchName(req.getBranchName());
        payload.setBranchCityId(req.getBranchCityId());
        payload.setBranchAddress(req.getBranchAddress());
        payload.setOnlineOnly(req.getOnlineOnly());
        payload.setEmail(blankToNull(req.getEmail()));
        payload.setPhone(blankToNull(req.getPhone()));
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            throw new InternalServerException(ErrorCode.REGISTRATION_PAYLOAD_ERROR);
        }
    }

    private BusinessRegistrationResult deserializeAndCreateBusiness(AppUser owner, String data) {
        try {
            BusinessRegistrationPayload payload = objectMapper.readValue(data, BusinessRegistrationPayload.class);
            return businessService.registerBusiness(
                    owner,
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

    private String authorityForSession(AppUser user, BusinessRegistrationResult bizResult) {
        if (user.getRole() == AppRole.CUSTOMER) {
            return "ROLE_CUSTOMER";
        }
        if (bizResult != null && bizResult.member() != null) {
            return "ROLE_BUSINESS_" + bizResult.member().getRole().name();
        }
        return "ROLE_BUSINESS_OWNER";
    }
}

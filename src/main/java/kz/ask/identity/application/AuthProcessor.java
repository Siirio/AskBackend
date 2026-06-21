package kz.ask.identity.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import kz.ask.business.domain.BusinessDomainService;
import kz.ask.business.domain.BusinessDomainService.BusinessRegistrationResult;
import kz.ask.identity.api.dto.AuthChallengeResponse;
import kz.ask.identity.api.dto.AuthSessionResponse;
import kz.ask.identity.api.dto.BusinessLoginStartRequest;
import kz.ask.identity.api.dto.BusinessRegisterRequest;
import kz.ask.identity.api.dto.CustomerLoginStartRequest;
import kz.ask.identity.api.dto.CustomerRegisterRequest;
import kz.ask.identity.api.dto.LogoutResponse;
import kz.ask.identity.api.dto.VerifyCodeRequest;
import kz.ask.identity.domain.IdentityDomainService;
import kz.ask.identity.domain.entity.AppUser;
import kz.ask.identity.domain.entity.AuthChallenge;
import kz.ask.identity.domain.entity.AuthSession;
import kz.ask.identity.domain.enums.AppRole;
import kz.ask.identity.domain.enums.AuthChallengeChannel;
import kz.ask.identity.domain.enums.AuthChallengePurpose;
import kz.ask.identity.domain.enums.UserStatus;
import kz.ask.identity.infrastructure.mail.EmailCodeSender;
import kz.ask.identity.infrastructure.security.AskPrincipal;
import kz.ask.identity.infrastructure.sms.SmsCodeSender;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class AuthProcessor {

    private final IdentityDomainService identityService;
    private final BusinessDomainService businessService;
    private final EmailCodeSender emailSender;
    private final SmsCodeSender smsSender;
    private final ObjectMapper objectMapper;

    public AuthProcessor(IdentityDomainService identityService,
                           BusinessDomainService businessService,
                           EmailCodeSender emailSender,
                           SmsCodeSender smsSender,
                           ObjectMapper objectMapper) {
        this.identityService = identityService;
        this.businessService = businessService;
        this.emailSender = emailSender;
        this.smsSender = smsSender;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public AuthChallengeResponse startCustomerLogin(CustomerLoginStartRequest req) {
        AppUser user = findUserForLogin(req.getEmail(), req.getPhone());
        if (user == null) {
            throw new AuthUserNotFoundException("Customer not found. Please register first.");
        }
        if (user.getRole() != AppRole.CUSTOMER) {
            throw new AuthRoleMismatchException("Account is not a customer account.");
        }
        return createLoginChallenge(user, req.getEmail(), req.getPhone(), req.isRememberMe());
    }

    @Transactional
    public AuthChallengeResponse registerCustomer(CustomerRegisterRequest req) {
        if (req.getEmail() != null && !req.getEmail().isBlank() && identityService.emailExists(req.getEmail())) {
            throw new AuthContactTakenException("Email already registered.");
        }
        if (req.getPhone() != null && !req.getPhone().isBlank() && identityService.phoneExists(req.getPhone())) {
            throw new AuthContactTakenException("Phone already registered.");
        }
        AppUser user = identityService.createUser(
                blankToNull(req.getEmail()),
                blankToNull(req.getPhone()),
                req.getDisplayName(),
                req.getPassword(),
                AppRole.CUSTOMER);
        return createRegisterChallenge(user, req.getEmail(), req.getPhone(), req.isRememberMe(), null);
    }

    @Transactional
    public AuthChallengeResponse startBusinessLogin(BusinessLoginStartRequest req) {
        AppUser user = findUserForLogin(req.getEmail(), req.getPhone());
        if (user == null) {
            throw new AuthUserNotFoundException("Business account not found. Please register first.");
        }
        if (user.getRole() != AppRole.BUSINESS) {
            throw new AuthRoleMismatchException("Account is not a business account.");
        }
        return createLoginChallenge(user, req.getEmail(), req.getPhone(), req.isRememberMe());
    }

    @Transactional
    public AuthChallengeResponse registerBusiness(BusinessRegisterRequest req) {
        if (req.getEmail() != null && !req.getEmail().isBlank() && identityService.emailExists(req.getEmail())) {
            throw new AuthContactTakenException("Email already registered.");
        }
        if (req.getPhone() != null && !req.getPhone().isBlank() && identityService.phoneExists(req.getPhone())) {
            throw new AuthContactTakenException("Phone already registered.");
        }
        String email = blankToNull(req.getEmail());
        String phone = blankToNull(req.getPhone());
        AppUser user = identityService.createUser(email, phone, req.getBusinessName(), req.getPassword(), AppRole.BUSINESS);
        String registrationData = serializeBusinessRegistration(req);
        return createRegisterChallenge(user, email, phone, req.isRememberMe(), registrationData);
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

        AuthSession session = identityService.createSession(user, user.getRole(), challenge.isRememberMe());
        return AuthAssembler.toSessionResponse(session, user, bizResult);
    }

    public AuthSessionResponse currentSession(AskPrincipal principal) {
        AppUser user = identityService.findById(principal.getUserId());
        if (user == null || user.getStatus() != UserStatus.ACTIVE) {
            throw new AuthSessionInvalidException("Session invalid.");
        }
        BusinessRegistrationResult bizResult = null;
        if (user.getRole() == AppRole.BUSINESS) {
            bizResult = businessService.findByOwner(user.getId());
        }
        AuthSessionResponse resp = AuthAssembler.toSessionResponse(null, user, bizResult);
        resp.setAccessToken(null);
        return resp;
    }

    @Transactional
    public LogoutResponse logout(AskPrincipal principal) {
        identityService.logout(principal.getUserId());
        return new LogoutResponse(true);
    }

    private AuthChallengeResponse createLoginChallenge(AppUser user, String email, String phone, boolean rememberMe) {
        AuthChallengeChannel channel = email != null && !email.isBlank()
                ? AuthChallengeChannel.EMAIL : AuthChallengeChannel.SMS;
        String destination = email != null && !email.isBlank() ? email : phone;
        AuthChallenge challenge = identityService.createChallenge(
                user, email, phone, channel, AuthChallengePurpose.LOGIN, rememberMe, null);
        sendCode(channel, destination, challenge.getCodePlain());
        String masked = channel == AuthChallengeChannel.EMAIL
                ? identityService.maskEmail(destination) : identityService.maskPhone(destination);
        return AuthAssembler.toChallengeResponse(challenge, masked, user.getRole().name());
    }

    private AuthChallengeResponse createRegisterChallenge(AppUser user, String email, String phone,
                                                           boolean rememberMe, String registrationData) {
        AuthChallengeChannel channel = email != null && !email.isBlank()
                ? AuthChallengeChannel.EMAIL : AuthChallengeChannel.SMS;
        String destination = email != null && !email.isBlank() ? email : phone;
        AuthChallenge challenge = identityService.createChallenge(
                user, email, phone, channel, AuthChallengePurpose.REGISTER, rememberMe, registrationData);
        sendCode(channel, destination, challenge.getCodePlain());
        String masked = channel == AuthChallengeChannel.EMAIL
                ? identityService.maskEmail(destination) : identityService.maskPhone(destination);
        return AuthAssembler.toChallengeResponse(challenge, masked, user.getRole().name());
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
        payload.setOnlineOnly(req.isOnlineOnly());
        payload.setEmail(blankToNull(req.getEmail()));
        payload.setPhone(blankToNull(req.getPhone()));
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            throw new AuthRegistrationPayloadException("Business registration payload cannot be saved.");
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
                    payload.isOnlineOnly(),
                    payload.getEmail(),
                    payload.getPhone());
        } catch (JsonProcessingException e) {
            throw new AuthRegistrationPayloadException("Business registration payload cannot be read.");
        }
    }
}

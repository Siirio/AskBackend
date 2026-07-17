package kz.ask.identity.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import kz.ask.business.domain.BusinessMemberService;
import kz.ask.business.domain.BusinessService;
import kz.ask.business.domain.BranchMemberService;
import java.util.List;
import java.util.UUID;
import kz.ask.business.domain.dto.BusinessMemberDto;
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
import kz.ask.identity.api.dto.EmailAccountInfo;
import kz.ask.identity.api.dto.EmailInfoResponse;
import kz.ask.identity.api.dto.LogoutResponse;
import kz.ask.identity.api.dto.SwitchRoleRequest;
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
import kz.ask.shared.error.AuthException;
import kz.ask.shared.error.ConflictException;
import kz.ask.shared.error.ErrorCode;
import kz.ask.shared.error.ForbiddenException;
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
    private final BusinessMemberService businessMemberService;
    private final BranchMemberService branchMemberService;
    private final EmailCodeSender emailSender;
    private final ObjectMapper objectMapper;

    @Value("${auth.verification.test-mode:false}")
    private Boolean testMode;

    @Transactional
    public AuthChallengeResponse startCustomerLogin(CustomerLoginStartRequest req) {
        AppUserDto user = identityService.findAllActiveByEmail(req.getEmail()).stream()
                .filter(u -> u.getRole() == AppRole.CUSTOMER)
                .findFirst()
                .orElse(null);
        if (user == null) {
            throw new NotFoundException(ErrorCode.USER_NOT_FOUND);
        }
        return createLoginChallenge(user.getId(), user.getRole(), req.getEmail(), req.getRememberMe());
    }

    @Transactional
    public AuthChallengeResponse registerCustomer(CustomerRegisterRequest req) {
        if (identityService.emailExistsForRole(req.getEmail(), AppRole.CUSTOMER)) {
            throw new ConflictException(ErrorCode.EMAIL_ALREADY_REGISTERED);
        }
        String registrationData = serializeUserRegistration(
                req.getEmail(), req.getDisplayName(), req.getPassword(), "CUSTOMER");
        return createRegisterChallenge(null, AppRole.CUSTOMER, req.getEmail(), req.getRememberMe(), registrationData);
    }

    @Transactional
    public AuthChallengeResponse startBusinessLogin(BusinessLoginStartRequest req) {
        AppUserDto user = identityService.findAllActiveByEmail(req.getEmail()).stream()
                .filter(u -> isBusinessRole(u.getRole()))
                .findFirst()
                .orElse(null);
        if (user == null) {
            throw new NotFoundException(ErrorCode.USER_NOT_FOUND);
        }
        return createLoginChallenge(user.getId(), user.getRole(), req.getEmail(), req.getRememberMe());
    }

    @Transactional
    public AuthChallengeResponse registerBusiness(BusinessRegisterRequest req) {
        if (identityService.emailExistsForRole(req.getEmail(), AppRole.BUSINESS_OWNER)) {
            throw new ConflictException(ErrorCode.EMAIL_ALREADY_REGISTERED);
        }
        String registrationData = serializeBusinessRegistration(req.getEmail(),
                req.getBusinessName(), req.getPassword(), req);
        return createRegisterChallenge(null, AppRole.BUSINESS_OWNER, req.getEmail(), req.getRememberMe(), registrationData);
    }

    @Transactional
    public AuthSessionResponse verifyCode(VerifyCodeRequest req) {
        AuthChallengeDto challenge = identityService.verifyCode(req.getAuthChallengeId(), req.getCode());

        AppUserDto user;
        boolean isNewRegistration = challenge.getPurpose() == AuthChallengePurpose.REGISTER;
        if (isNewRegistration) {
            user = createUserFromStoredRegistration(challenge.getRegistrationData());
            identityService.activateUser(user.getId());
            user.setStatus(UserStatus.ACTIVE);
        } else {
            user = identityService.findById(challenge.getUserId());
        }
        identityService.recordLogin(user.getId());
        user = identityService.findById(user.getId());

        List<String> allRoles = identityService.findAllByEmail(user.getEmail()).stream()
                .filter(u -> u.getStatus() == UserStatus.ACTIVE)
                .map(u -> u.getRole().name())
                .distinct()
                .toList();

        BusinessRegistrationResult bizResult = null;
        if (isNewRegistration
                && isBusinessRole(user.getRole())
                && challenge.getRegistrationData() != null) {
            bizResult = deserializeAndCreateBusiness(user, challenge.getRegistrationData());
        } else if (isBusinessRole(user.getRole())) {
            bizResult = resolveBusinessContext(user);
        }

        String authority = authorityForSession(user, bizResult);
        AuthSessionDto session = identityService.createSession(user.getId(), authority, challenge.getRememberMe());

        AuthSessionResponse response = buildSessionResponse(session, user, bizResult);
        response.setAllRoles(allRoles);
        if (isNewRegistration && allRoles.size() == 1) {
            response.setSuggestRoleExpansion(true);
        }
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
                req.getDisplayName(), req.getEmail());
        return currentSession(principal);
    }

    @Transactional
    public LogoutResponse logout(AskPrincipal principal) {
        identityService.logout(principal.getUserId());
        return LogoutResponse.builder().success(true).build();
    }

    @Transactional
    public AuthSessionResponse switchRole(AskPrincipal principal, SwitchRoleRequest req) {
        AppUserDto currentUser = identityService.findById(principal.getUserId());
        if (currentUser == null) {
            throw new UnauthorizedException(ErrorCode.SESSION_INVALID);
        }

        AppRole targetRole;
        try {
            targetRole = AppRole.valueOf(req.getRole());
        } catch (IllegalArgumentException e) {
            throw new ValidationException(ErrorCode.ROLE_MISMATCH);
        }

        AppUserDto targetUser = identityService.findByEmailAndRole(currentUser.getEmail(), targetRole);
        if (targetUser == null || targetUser.getStatus() != UserStatus.ACTIVE) {
            throw new NotFoundException(ErrorCode.USER_NOT_FOUND);
        }

        identityService.logout(principal.getUserId());
        identityService.recordLogin(targetUser.getId());

        BusinessRegistrationResult bizResult = resolveBusinessContext(targetUser);

        String authority = authorityForSession(targetUser, bizResult);
        AuthSessionDto session = identityService.createSession(targetUser.getId(), authority, false);
        AuthSessionResponse response = buildSessionResponse(session, targetUser, bizResult);
        response.setAllRoles(identityService.findAllByEmail(currentUser.getEmail()).stream()
                .filter(u -> u.getStatus() == UserStatus.ACTIVE)
                .map(u -> u.getRole().name())
                .distinct()
                .toList());
        return response;
    }

    @Transactional
    public AuthSessionResponse changePassword(AskPrincipal principal, ChangePasswordRequest req) {
        if (!req.getNewPassword().equals(req.getPasswordConfirmation())) {
            throw new ValidationException(ErrorCode.PASSWORDS_DO_NOT_MATCH);
        }

        AppUserDto currentUser = identityService.findById(principal.getUserId());

        AppRole targetRole;
        try {
            targetRole = AppRole.valueOf(req.getRole());
        } catch (IllegalArgumentException e) {
            throw new ValidationException(ErrorCode.ROLE_MISMATCH);
        }

        AppUserDto targetUser = identityService.findByEmailAndRole(currentUser.getEmail(), targetRole);
        if (targetUser == null) {
            throw new NotFoundException(ErrorCode.USER_NOT_FOUND);
        }

        if (!identityService.verifyPassword(req.getCurrentPassword(), targetUser.getPasswordHash())) {
            throw new AuthException(ErrorCode.INVALID_CREDENTIALS);
        }

        identityService.changePassword(targetUser.getId(), req.getNewPassword());
        identityService.logout(principal.getUserId());
        identityService.recordLogin(targetUser.getId());

        BusinessRegistrationResult bizResult = resolveBusinessContext(targetUser);

        String authority = authorityForSession(targetUser, bizResult);
        AuthSessionDto session = identityService.createSession(targetUser.getId(), authority, false);
        AuthSessionResponse response = buildSessionResponse(session, targetUser, bizResult);
        response.setAllRoles(identityService.findAllByEmail(currentUser.getEmail()).stream()
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

    public EmailInfoResponse emailInfo(String email) {
        List<AppUserDto> users = identityService.findAllByEmail(email);
        if (users.isEmpty()) {
            return EmailInfoResponse.builder().exists(false).accounts(List.of()).build();
        }
        List<EmailAccountInfo> accounts = users.stream()
                .map(u -> {
                    String businessName = null;
                    if (isBusinessRole(u.getRole())) {
                        BusinessRegistrationResult biz = businessService.findByOwner(u.getId());
                        if (biz == null) {
                            biz = businessService.findByMember(u.getId());
                        }
                        if (biz != null) {
                            businessName = biz.getBusiness().getName();
                        }
                    }
                    return EmailAccountInfo.builder()
                            .role(u.getRole().name())
                            .status(u.getStatus().name())
                            .businessName(businessName)
                            .build();
                })
                .toList();
        return EmailInfoResponse.builder().exists(true).accounts(accounts).build();
    }

    private AuthChallengeResponse createLoginChallenge(UUID userId, AppRole role, String email, Boolean rememberMe) {
        AuthChallengeDto challenge = identityService.createChallenge(
                userId, email, AuthChallengeChannel.EMAIL, AuthChallengePurpose.LOGIN, rememberMe, null);
        if (Boolean.TRUE.equals(testMode)) {
            return buildChallengeResponse(challenge, email, role.name(), challenge.getCodePlain());
        }
        emailSender.sendCode(email, challenge.getCodePlain());
        String masked = identityService.maskEmail(email);
        return buildChallengeResponse(challenge, masked, role.name(), null);
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

    private String serializeUserRegistration(String email, String displayName,
                                             String password, String role) {
        BusinessRegistrationPayload payload = new BusinessRegistrationPayload();
        payload.setEmail(email);
        payload.setDisplayName(displayName);
        payload.setPassword(password);
        payload.setRole(role);
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            throw new InternalServerException(ErrorCode.REGISTRATION_PAYLOAD_ERROR);
        }
    }

    private String serializeBusinessRegistration(String email, String displayName,
                                                  String password, BusinessRegisterRequest req) {
        BusinessRegistrationPayload payload = new BusinessRegistrationPayload();
        payload.setEmail(email);
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
            AppRole role = "BUSINESS".equals(payload.getRole()) ? AppRole.BUSINESS_OWNER : AppRole.CUSTOMER;
            return identityService.createUser(
                    payload.getEmail(), payload.getDisplayName(),
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
                    payload.getEmail());
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
        BusinessMemberDto member = businessMemberService.findByUser(user.getId());
        if (member != null) {
            return "ROLE_BUSINESS_" + member.getRole();
        }
        if (branchMemberService.isBranchStaff(user.getId())) {
            return "ROLE_BUSINESS_WORKER";
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
        if (user.getRole() == AppRole.CUSTOMER) {
            return "CLIENT_SEARCH";
        }
        if (authority != null) {
            if (authority.contains("OWNER") || authority.contains("MANAGER")) {
                return "OWNER_BRANCHES";
            }
            if (authority.contains("WORKER")) {
                return "BRANCH_WORKSPACE";
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

    private boolean isBusinessRole(AppRole role) {
        return role == AppRole.BUSINESS_OWNER
                || role == AppRole.BUSINESS_MANAGER
                || role == AppRole.BUSINESS_WORKER;
    }

    private BusinessRegistrationResult resolveBusinessContext(AppUserDto user) {
        if (!isBusinessRole(user.getRole())) {
            return null;
        }
        BusinessRegistrationResult bizResult = businessService.findByOwner(user.getId());
        if (bizResult != null) {
            return bizResult;
        }
        return businessService.findByMember(user.getId());
    }
}

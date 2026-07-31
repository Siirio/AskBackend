package kz.ask.identity.infrastructure.oauth2;

import java.security.SecureRandom;
import java.util.HexFormat;
import java.util.Map;
import java.util.UUID;
import kz.ask.identity.domain.IdentityService;
import kz.ask.identity.domain.dto.AppUserDto;
import kz.ask.identity.authorization.domain.enums.Role;
import kz.ask.identity.domain.enums.UserStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    static final String USER_ID_ATTRIBUTE = "ask_user_id";
    static final String REGISTRATION_REQUIRED_ATTRIBUTE = "ask_registration_required";

    private final IdentityService identityService;
    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        Boolean emailVerified = oAuth2User.getAttribute("email_verified");
        if (email == null || email.isBlank() || !Boolean.TRUE.equals(emailVerified)) {
            log.warn("Google OAuth rejected: email={}, emailVerified={}", email, emailVerified);
            throw oauthError("Google account must provide a verified email");
        }
        if (name == null || name.isBlank()) {
            int separator = email.indexOf('@');
            name = separator > 0 ? email.substring(0, separator) : email;
        }

        AppUserDto user = identityService.findAllByEmail(email).stream()
                .findFirst()
                .orElse(null);
        boolean registrationRequired = user == null;
        if (user == null) {
            byte[] randomBytes = new byte[32];
            secureRandom.nextBytes(randomBytes);
            String randomPassword = HexFormat.of().formatHex(randomBytes);
            user = identityService.createUser(email, name, randomPassword, Role.CUSTOMER);
            identityService.activateUser(user.getId());
            user = identityService.findById(user.getId());
        } else if (user.getStatus() == UserStatus.BLOCKED || user.getStatus() == UserStatus.DELETED) {
            log.warn("Google OAuth rejected: user {} status is {}", user.getId(), user.getStatus());
            throw oauthError("Ask account is not active");
        } else if (user.getStatus() != UserStatus.ACTIVE) {
            identityService.activateUser(user.getId());
            user = identityService.findById(user.getId());
        }
        identityService.recordLogin(user.getId());

        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes != null) {
            requestAttributes.setAttribute(USER_ID_ATTRIBUTE, user.getId().toString(), RequestAttributes.SCOPE_REQUEST);
            requestAttributes.setAttribute(REGISTRATION_REQUIRED_ATTRIBUTE, registrationRequired, RequestAttributes.SCOPE_REQUEST);
        }

        Map<String, Object> attributes = new java.util.HashMap<>(oAuth2User.getAttributes());
        attributes.put(USER_ID_ATTRIBUTE, user.getId().toString());
        attributes.put(REGISTRATION_REQUIRED_ATTRIBUTE, registrationRequired);

        return new DefaultOAuth2User(
                oAuth2User.getAuthorities(),
                attributes,
                "email"
        );
    }

    private OAuth2AuthenticationException oauthError(String description) {
        return new OAuth2AuthenticationException(
                new OAuth2Error("invalid_google_account"),
                description);
    }
}

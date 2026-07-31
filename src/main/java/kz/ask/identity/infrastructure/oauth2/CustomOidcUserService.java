package kz.ask.identity.infrastructure.oauth2;

import java.security.SecureRandom;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.Map;
import kz.ask.identity.domain.IdentityService;
import kz.ask.identity.domain.dto.AppUserDto;
import kz.ask.identity.authorization.domain.enums.Role;
import kz.ask.identity.domain.enums.UserStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomOidcUserService implements OAuth2UserService<OidcUserRequest, OidcUser> {

    private final OidcUserService delegate = new OidcUserService();
    private final IdentityService identityService;
    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        OidcUser oidcUser = delegate.loadUser(userRequest);

        String email = oidcUser.getAttribute("email");
        String name = oidcUser.getAttribute("name");
        Boolean emailVerified = oidcUser.getAttribute("email_verified");
        if (email == null || email.isBlank() || !Boolean.TRUE.equals(emailVerified)) {
            log.warn("Google OIDC rejected: email={}, emailVerified={}", email, emailVerified);
            throw new OAuth2AuthenticationException(
                    new OAuth2Error("invalid_google_account"),
                    "Google account must provide a verified email");
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
            log.warn("Google OIDC rejected: user {} status is {}", user.getId(), user.getStatus());
            throw new OAuth2AuthenticationException(
                    new OAuth2Error("invalid_google_account"),
                    "Ask account is not active");
        } else if (user.getStatus() != UserStatus.ACTIVE) {
            identityService.activateUser(user.getId());
            user = identityService.findById(user.getId());
        }
        identityService.recordLogin(user.getId());

        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes != null) {
            requestAttributes.setAttribute(CustomOAuth2UserService.USER_ID_ATTRIBUTE, user.getId().toString(), RequestAttributes.SCOPE_REQUEST);
            requestAttributes.setAttribute(CustomOAuth2UserService.REGISTRATION_REQUIRED_ATTRIBUTE, registrationRequired, RequestAttributes.SCOPE_REQUEST);
        }

        Map<String, Object> attributes = new HashMap<>(oidcUser.getAttributes());
        attributes.put(CustomOAuth2UserService.USER_ID_ATTRIBUTE, user.getId().toString());
        attributes.put(CustomOAuth2UserService.REGISTRATION_REQUIRED_ATTRIBUTE, registrationRequired);

        return new EnrichedOidcUser(oidcUser, attributes);
    }

    private static class EnrichedOidcUser implements OidcUser {

        private final OidcUser delegate;
        private final Map<String, Object> attributes;

        EnrichedOidcUser(OidcUser delegate, Map<String, Object> attributes) {
            this.delegate = delegate;
            this.attributes = Collections.unmodifiableMap(attributes);
        }

        @Override
        public Map<String, Object> getAttributes() {
            return attributes;
        }

        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() {
            return delegate.getAuthorities();
        }

        @Override
        public String getName() {
            return delegate.getName();
        }

        @Override
        public Map<String, Object> getClaims() {
            return delegate.getClaims();
        }

        @Override
        public OidcIdToken getIdToken() {
            return delegate.getIdToken();
        }

        @Override
        public OidcUserInfo getUserInfo() {
            return delegate.getUserInfo();
        }
    }
}

package kz.ask.identity.infrastructure.oauth2;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import kz.ask.identity.domain.IdentityService;
import org.springframework.web.util.UriComponentsBuilder;
import kz.ask.identity.domain.dto.AppUserDto;
import kz.ask.identity.domain.dto.AuthSessionDto;
import kz.ask.identity.infrastructure.security.AuthCookieService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

@Component
@RequiredArgsConstructor
public class OAuth2AuthSuccessHandler implements AuthenticationSuccessHandler {

    private final IdentityService identityService;
    private final AuthCookieService authCookieService;

    @Value("${auth.oauth2.frontend-redirect-uri:http://localhost:5173/oauth/callback}")
    private String frontendRedirectUri;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        String userId = requestAttributes != null
                ? (String) requestAttributes.getAttribute(CustomOAuth2UserService.USER_ID_ATTRIBUTE, RequestAttributes.SCOPE_REQUEST)
                : null;
        if (userId == null) {
            OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
            String email = oAuth2User.getAttribute("email");
            throw new IllegalStateException("Google OAuth user ID not in request attributes for email: " + email);
        }

        AppUserDto user = identityService.findById(UUID.fromString(userId));
        AuthSessionDto session = identityService.createSession(user.getId(), "ROLE_USER", true);

        Boolean registrationRequired = requestAttributes != null
                ? (Boolean) requestAttributes.getAttribute(CustomOAuth2UserService.REGISTRATION_REQUIRED_ATTRIBUTE, RequestAttributes.SCOPE_REQUEST)
                : null;
        if (registrationRequired == null) {
            OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
            registrationRequired = oAuth2User.getAttribute(CustomOAuth2UserService.REGISTRATION_REQUIRED_ATTRIBUTE);
        }

        response.setHeader("Cache-Control", "no-store");
        response.setHeader("Referrer-Policy", "no-referrer");
        authCookieService.write(response, session.getPlainToken(), session.getExpiresAt());

        String redirectUri = frontendRedirectUri;
        if (Boolean.TRUE.equals(registrationRequired)) {
            redirectUri = UriComponentsBuilder.fromUriString(frontendRedirectUri)
                    .queryParam("registration", "1")
                    .build()
                    .toUriString();
        }
        response.sendRedirect(redirectUri);
    }
}

package kz.ask.identity.infrastructure.oauth2;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import kz.ask.identity.domain.IdentityService;
import kz.ask.identity.domain.dto.AppUserDto;
import kz.ask.identity.domain.dto.AuthSessionDto;
import kz.ask.identity.infrastructure.security.AuthCookieService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

@Component
@RequiredArgsConstructor
public class OAuth2AuthSuccessHandler implements AuthenticationSuccessHandler {

    private static final String REGISTRATION_QUERY_PARAMETER = "registration";
    private static final String REGISTRATION_QUERY_VALUE = "1";

    private final IdentityService identityService;
    private final AuthCookieService authCookieService;

    @Value("${auth.oauth2.frontend-redirect-uri:http://localhost:5173/oauth/callback}")
    private String frontendRedirectUri;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String userId = oAuth2User.getAttribute(CustomOAuth2UserService.USER_ID_ATTRIBUTE);
        Boolean registrationRequired = oAuth2User.getAttribute(
                CustomOAuth2UserService.REGISTRATION_REQUIRED_ATTRIBUTE);
        AppUserDto user = identityService.findById(UUID.fromString(userId));

        AuthSessionDto session = identityService.createSession(user.getId(), "ROLE_USER", true);

        response.setHeader("Cache-Control", "no-store");
        response.setHeader("Referrer-Policy", "no-referrer");
        authCookieService.write(response, session.getPlainToken(), session.getExpiresAt());
        String redirectUri = Boolean.TRUE.equals(registrationRequired)
                ? UriComponentsBuilder.fromUriString(frontendRedirectUri)
                        .queryParam(REGISTRATION_QUERY_PARAMETER, REGISTRATION_QUERY_VALUE)
                        .build()
                        .encode()
                        .toUriString()
                : frontendRedirectUri;
        response.sendRedirect(redirectUri);
    }
}

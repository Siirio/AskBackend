package kz.ask.identity.infrastructure.oauth2;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import kz.ask.identity.domain.IdentityService;
import kz.ask.identity.domain.dto.AppUserDto;
import kz.ask.identity.domain.dto.AuthSessionDto;
import kz.ask.identity.domain.enums.AppRole;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OAuth2AuthSuccessHandler implements AuthenticationSuccessHandler {

    private final IdentityService identityService;

    @Value("${ask.cors.allowed-origins:http://localhost:5173}")
    private String allowedOrigins;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String userId = oAuth2User.getAttribute("ask_user_id");
        AppUserDto user = identityService.findById(UUID.fromString(userId));

        String authority = user.getRole() == AppRole.CUSTOMER ? "ROLE_CUSTOMER" : "ROLE_BUSINESS_OWNER";
        AuthSessionDto session = identityService.createSession(user.getId(), authority, true);

        String frontendUrl = allowedOrigins.split(",")[0].trim();
        String redirectUrl = frontendUrl + "/oauth/callback?token=" + session.getPlainToken();
        response.sendRedirect(redirectUrl);
    }
}

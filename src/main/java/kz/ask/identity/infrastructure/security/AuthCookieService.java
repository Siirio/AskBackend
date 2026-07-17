package kz.ask.identity.infrastructure.security;

import jakarta.servlet.http.HttpServletResponse;
import java.time.Duration;
import java.time.Instant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
public class AuthCookieService {

    private final String cookieName;
    private final String cookiePath;
    private final String sameSite;
    private final Boolean secure;

    public AuthCookieService(@Value("${auth.cookie.name:ASK_SESSION}") String cookieName,
                             @Value("${auth.cookie.path:/}") String cookiePath,
                             @Value("${auth.cookie.same-site:Lax}") String sameSite,
                             @Value("${auth.cookie.secure:false}") Boolean secure) {
        this.cookieName = cookieName;
        this.cookiePath = cookiePath;
        this.sameSite = sameSite;
        this.secure = secure;
    }

    public String getCookieName() {
        return cookieName;
    }

    public void write(HttpServletResponse response, String token, Instant expiresAt) {
        Duration maxAge = Duration.between(Instant.now(), expiresAt);
        ResponseCookie cookie = ResponseCookie.from(cookieName, token)
                .httpOnly(true)
                .secure(Boolean.TRUE.equals(secure))
                .sameSite(sameSite)
                .path(cookiePath)
                .maxAge(maxAge.isNegative() ? Duration.ZERO : maxAge)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    public void clear(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from(cookieName, "")
                .httpOnly(true)
                .secure(Boolean.TRUE.equals(secure))
                .sameSite(sameSite)
                .path(cookiePath)
                .maxAge(Duration.ZERO)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}

package kz.ask.identity.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Cookie;
import java.io.IOException;
import java.util.List;
import kz.ask.identity.domain.IdentityService;
import kz.ask.identity.domain.dto.AuthSessionDto;
import org.springframework.http.HttpMethod;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final String SESSION_EXCHANGE_PATH = "/api/v1/auth/session";

    private final IdentityService identityService;
    private final AuthCookieService authCookieService;
    private final JwtTokenService jwtTokenService;

    public JwtAuthFilter(IdentityService identityService,
                         AuthCookieService authCookieService,
                         JwtTokenService jwtTokenService) {
        this.identityService = identityService;
        this.authCookieService = authCookieService;
        this.jwtTokenService = jwtTokenService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                     HttpServletResponse response,
                                     FilterChain filterChain) throws ServletException, IOException {
        String token = resolveToken(request);
        if (token == null) {
            filterChain.doFilter(request, response);
            return;
        }
        AuthSessionDto session = resolveSession(token);
        if (session == null) {
            filterChain.doFilter(request, response);
            return;
        }
        String authority = session.getAuthority();
        List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(authority));

        AskPrincipal principal = new AskPrincipal(session.getUserId(), session.getId(), session.getUserDisplayName(), authority);
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(principal, null, authorities);
        auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(auth);
        filterChain.doFilter(request, response);
    }

    private AuthSessionDto resolveSession(String token) {
        AuthSessionDto opaqueSession = identityService.findSessionByToken(token);
        if (opaqueSession != null) {
            return opaqueSession;
        }
        Jwt jwt = jwtTokenService.decode(token);
        if (jwt == null) {
            return null;
        }
        java.util.UUID sessionId = jwtTokenService.sessionId(jwt);
        if (sessionId == null) {
            return null;
        }
        AuthSessionDto session = identityService.findSessionById(sessionId);
        if (session == null || !session.getUserId().toString().equals(jwt.getSubject())) {
            return null;
        }
        return session;
    }

    private String resolveToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        if (!HttpMethod.GET.matches(request.getMethod())
                || !SESSION_EXCHANGE_PATH.equals(request.getRequestURI())) {
            return null;
        }
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        for (Cookie cookie : cookies) {
            if (authCookieService.getCookieName().equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }
}

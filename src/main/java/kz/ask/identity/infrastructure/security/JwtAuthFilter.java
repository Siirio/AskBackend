package kz.ask.identity.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import kz.ask.identity.domain.IdentityService;
import kz.ask.identity.domain.dto.AuthSessionDto;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final IdentityService identityService;

    public JwtAuthFilter(IdentityService identityService) {
        this.identityService = identityService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                     HttpServletResponse response,
                                     FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }
        String token = header.substring(7);
        AuthSessionDto session = identityService.findSessionByToken(token);
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
}

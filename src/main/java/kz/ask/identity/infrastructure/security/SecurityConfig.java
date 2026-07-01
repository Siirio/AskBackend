package kz.ask.identity.infrastructure.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(withDefaults())
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/swagger-ui/**",
                    "/swagger-ui.html",
                    "/v3/api-docs/**"
                ).permitAll()
                .requestMatchers(HttpMethod.GET,
                    "/api/v1/search",
                    "/api/v1/cities/resolve",
                    "/api/v1/cities",
                    "/api/v1/categories",
                    "/api/v1/categories/*/subcategories",
                    "/api/v1/businesses/*/brand-profile",
                    "/api/v1/businesses/*/storefront",
                    "/api/v1/businesses/*/drops"
                ).permitAll()
                .requestMatchers(HttpMethod.POST,
                    "/api/v1/search",
                    "/api/v1/search/intent-structure",
                    "/api/v1/auth/login",
                    "/api/v1/auth/customer/login/start",
                    "/api/v1/auth/customer/register",
                    "/api/v1/auth/business/login/start",
                    "/api/v1/auth/business/register",
                    "/api/v1/auth/verify"
                ).permitAll()
                .requestMatchers("/api/v1/auth/change-temporary-password").authenticated()
                .requestMatchers("/api/v1/auth/session").authenticated()
                .requestMatchers("/api/v1/auth/logout").authenticated()
                .requestMatchers("/api/v1/**").authenticated()
                .anyRequest().permitAll()
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

}

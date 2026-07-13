package kz.ask.identity.infrastructure.security;

import kz.ask.identity.infrastructure.oauth2.CustomOAuth2UserService;
import kz.ask.identity.infrastructure.oauth2.OAuth2AuthSuccessHandler;
import org.springframework.beans.factory.annotation.Value;
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
    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2AuthSuccessHandler oAuth2AuthSuccessHandler;

    @Value("${OAUTH2_GOOGLE_CLIENT_ID:}")
    private String oauth2GoogleClientId;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter,
                          CustomOAuth2UserService customOAuth2UserService,
                          OAuth2AuthSuccessHandler oAuth2AuthSuccessHandler) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.customOAuth2UserService = customOAuth2UserService;
        this.oAuth2AuthSuccessHandler = oAuth2AuthSuccessHandler;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(withDefaults())
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        if (oauth2GoogleClientId != null && !oauth2GoogleClientId.isBlank()) {
            http.oauth2Login(oauth2 -> oauth2
                .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))
                .successHandler(oAuth2AuthSuccessHandler)
            );
        }

        http
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
                    "/api/v1/businesses/*/drops",
                    "/api/v1/chat/files/*",
                    "/api/v1/auth/email-info"
                ).permitAll()
                .requestMatchers(HttpMethod.POST,
                    "/api/v1/search",
                    "/api/v1/auth/login",
                    "/api/v1/auth/customer/login/start",
                    "/api/v1/auth/customer/register",
                    "/api/v1/auth/business/login/start",
                    "/api/v1/auth/business/register",
                    "/api/v1/auth/verify",
                    "/api/v1/auth/select-role"
                ).permitAll()
                .requestMatchers(
                    "/oauth2/**",
                    "/login/oauth2/**"
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

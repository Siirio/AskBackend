package kz.ask.identity.api.dto;

import java.time.Instant;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class AuthSessionResponse {

    private String accessToken;
    private String tokenType;
    private Instant expiresAt;
    private Boolean remembered;
    private Boolean activationRequired;
    private String role;
    private String startRoute;
    private AuthUserResponse user;
    private AuthBusinessContextResponse business;
}

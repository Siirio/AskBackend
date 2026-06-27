package kz.ask.identity.api.dto;

import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class AuthChallengeResponse {

    private UUID authChallengeId;
    private String role;
    private String purpose;
    private String channel;
    private String maskedDestination;
    private Instant expiresAt;
    private String code;
}

package kz.ask.identity.domain.dto;

import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class AuthSessionDto {

    private UUID id;
    private UUID userId;
    private String userDisplayName;
    private String tokenHash;
    private String authority;
    private Boolean remembered;
    private Boolean activationRequired;
    private Instant expiresAt;
    private Instant revokedAt;
    private String plainToken;
}

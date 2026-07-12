package kz.ask.identity.domain.dto;

import java.time.Instant;
import java.util.UUID;
import kz.ask.identity.domain.enums.AuthChallengeChannel;
import kz.ask.identity.domain.enums.AuthChallengePurpose;
import kz.ask.identity.domain.enums.AuthChallengeStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class AuthChallengeDto {

    private UUID id;
    private UUID userId;
    private String email;
    private AuthChallengeChannel channel;
    private AuthChallengePurpose purpose;
    private String codeHash;
    private Integer attempts;
    private Integer maxAttempts;
    private Instant expiresAt;
    private AuthChallengeStatus status;
    private Boolean rememberMe;
    private String registrationData;
    private String codePlain;
}

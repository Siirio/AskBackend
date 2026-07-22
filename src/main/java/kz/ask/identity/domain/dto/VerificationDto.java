package kz.ask.identity.domain.dto;

import java.time.Instant;
import java.util.UUID;
import kz.ask.identity.domain.enums.VerificationChannel;
import kz.ask.identity.domain.enums.VerificationPurpose;
import kz.ask.identity.domain.enums.VerificationStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class VerificationDto {

    private UUID id;
    private UUID userId;
    private String email;
    private VerificationChannel channel;
    private VerificationPurpose purpose;
    private String codeHash;
    private Integer attempts;
    private Integer maxAttempts;
    private Instant expiresAt;
    private VerificationStatus status;
    private Boolean isRememberMe;
    private String registrationData;
    private String codePlain;
}

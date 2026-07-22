package kz.ask.moderation.api.dto;

import java.time.Instant;
import java.util.UUID;
import kz.ask.business.domain.enums.VerificationStatus;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class VerificationListResponse {

    private UUID businessId;
    private String businessName;
    private VerificationStatus status;
    private Instant createdAt;
    private Instant updatedAt;
}

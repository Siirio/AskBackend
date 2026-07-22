package kz.ask.business.api.dto;

import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BusinessCatalogStatusResponse {

    private UUID businessId;
    private String catalogStatus;
    private Instant deadlineAt;
    private String verificationStatus;
    private Instant catalogSetupStartedAt;
    private Instant catalogSetupDeadlineAt;
    private Instant catalogSetupCompletedAt;
    private Boolean catalogReady;
}

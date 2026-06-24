package kz.ask.service.api.dto;

import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FixServiceBookingResponse {

    private UUID requestId;
    private UUID branchId;
    private String customerRequestStatus;
    private String supplierResponseStatus;
    private ActivityDisplayStatus activityDisplayStatus;
    private Instant requestedStartAt;
    private Instant proposedStartAt;
    private Instant confirmedStartAt;
    private Instant confirmedEndAt;
    private String providerNote;
}

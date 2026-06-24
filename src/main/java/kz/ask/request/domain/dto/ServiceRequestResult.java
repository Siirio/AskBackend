package kz.ask.request.domain.dto;

import java.time.Instant;
import java.util.UUID;
import kz.ask.request.domain.enums.CustomerRequestStatus;
import kz.ask.request.domain.enums.SupplierResponseStatus;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ServiceRequestResult {

    private UUID requestId;
    private UUID branchId;
    private CustomerRequestStatus customerRequestStatus;
    private SupplierResponseStatus supplierResponseStatus;
    private Instant requestedStartAt;
    private Instant proposedStartAt;
    private Instant confirmedStartAt;
    private Instant confirmedEndAt;
    private String providerNote;

    private boolean shouldCreateBooking;
    private UUID customerId;
    private UUID serviceBranchOfferId;
}

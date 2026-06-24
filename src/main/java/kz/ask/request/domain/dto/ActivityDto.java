package kz.ask.request.domain.dto;

import java.time.Instant;
import java.util.UUID;
import kz.ask.request.domain.enums.CustomerRequestStatus;
import kz.ask.request.domain.enums.SupplierResponseStatus;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ActivityDto {

    private UUID activityId;
    private String type;
    private String requestText;
    private UUID branchId;
    private String branchAddress;
    private String customerName;
    private String customerContact;
    private Instant requestedStartAt;
    private Instant proposedStartAt;
    private Instant confirmedStartAt;
    private Instant confirmedEndAt;
    private CustomerRequestStatus customerRequestStatus;
    private SupplierResponseStatus supplierResponseStatus;
    private Integer unreadCount;
}

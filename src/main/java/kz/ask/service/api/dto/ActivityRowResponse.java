package kz.ask.service.api.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ActivityRowResponse {

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
    private ActivityDisplayStatus activityDisplayStatus;
    private String customerRequestStatus;
    private String supplierResponseStatus;
    private Integer unreadCount;
    private List<String> actions;
}

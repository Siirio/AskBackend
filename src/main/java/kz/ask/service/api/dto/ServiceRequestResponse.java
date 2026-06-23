package kz.ask.service.api.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Setter
@Getter
public class ServiceRequestResponse {
    private UUID requestId;
    private UUID branchId;
    private String status;
    private Instant confirmedStartAt;
    private Instant confirmedEndAt;
    private String providerNote;
}

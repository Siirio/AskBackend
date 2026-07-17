package kz.ask.business.api.dto;

import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BusinessCatalogStatusResponse {

    private UUID businessId;
    private String status;
    private Instant deadlineAt;
}

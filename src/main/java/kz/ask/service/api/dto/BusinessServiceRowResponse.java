package kz.ask.service.api.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
public class BusinessServiceRowResponse {
    private UUID serviceOfferingId;
    private UUID serviceBranchOfferId;
    private UUID branchId;
    private UUID categoryId;
    private String name;
    private String description;
    private BigDecimal basePrice;
    private Integer durationMinutes;
    private String scheduleText;
    private Boolean active;
    private Instant updatedAt;
}

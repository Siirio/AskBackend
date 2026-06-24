package kz.ask.service.domain.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ServiceBranchOfferDto {

    private UUID serviceOfferingId;
    private UUID serviceBranchOfferId;
    private UUID businessId;
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

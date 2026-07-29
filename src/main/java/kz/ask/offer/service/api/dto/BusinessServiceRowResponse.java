package kz.ask.offer.service.api.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import java.util.Map;
import kz.ask.offer.service.domain.enums.ServiceMode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BusinessServiceRowResponse {

    private UUID serviceOfferingId;
    private UUID branchId;
    private UUID categoryId;
    private String categoryLabel;
    private String name;
    private String description;
    private ServiceMode serviceMode;
    private BigDecimal basePrice;
    private String scheduleText;
    private Map<String, Object> attributes;
    private Boolean isActive;
    private Instant updatedAt;
}

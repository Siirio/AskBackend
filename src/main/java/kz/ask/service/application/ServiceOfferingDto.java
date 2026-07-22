package kz.ask.service.application;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import kz.ask.service.domain.enums.ServiceMode;
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
public class ServiceOfferingDto {

    private UUID id;
    private UUID businessId;
    private UUID branchId;
    private String categoryLabel;
    private String name;
    private String description;
    private ServiceMode serviceMode;
    private BigDecimal basePrice;
    private String scheduleText;
    private Boolean active;
    private Instant updatedAt;
}

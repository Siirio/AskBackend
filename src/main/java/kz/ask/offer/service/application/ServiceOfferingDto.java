package kz.ask.offer.service.application;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import java.util.List;
import java.util.Map;
import kz.ask.offer.service.domain.enums.ServiceMode;
import kz.ask.offer.purchase.domain.PurchaseDestinationDto;
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
    private UUID categoryId;
    private String categoryLabel;
    private String name;
    private String description;
    private List<String> imageFiles;
    private List<PurchaseDestinationDto> purchaseDestinations;
    private ServiceMode serviceMode;
    private BigDecimal basePrice;
    private String scheduleText;
    private Map<String, Object> attributes;
    private Boolean isActive;
    private Instant updatedAt;
}

package kz.ask.offer.service.api.dto;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;
import java.util.List;
import jakarta.validation.Valid;
import kz.ask.offer.purchase.api.dto.PurchaseDestinationRequest;
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
public class BusinessServiceUpdateRequest {

    private UUID categoryId;
    private String categoryName;
    private UUID branchId;

    private String name;

    private String description;

    private List<@Valid PurchaseDestinationRequest> purchaseDestinations;

    private ServiceMode serviceMode;

    private BigDecimal basePrice;

    private String scheduleText;

    private Boolean isActive;

    private Map<String, Object> attributes;
}

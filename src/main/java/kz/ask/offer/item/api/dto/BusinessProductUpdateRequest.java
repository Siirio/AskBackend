package kz.ask.offer.item.api.dto;

import jakarta.validation.constraints.DecimalMin;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import jakarta.validation.Valid;
import kz.ask.offer.purchase.api.dto.PurchaseDestinationRequest;
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
public class BusinessProductUpdateRequest {

    private UUID categoryId;
    private String categoryName;
    private UUID branchId;

    private String name;

    private String description;

    private List<@Valid PurchaseDestinationRequest> purchaseDestinations;

    private List<String> tags;

    private Map<String, Object> attributes;

    @DecimalMin(value = "0", inclusive = true)
    private BigDecimal price;

    private Boolean isActive;
}

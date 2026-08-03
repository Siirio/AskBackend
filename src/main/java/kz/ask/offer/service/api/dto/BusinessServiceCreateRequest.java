package kz.ask.offer.service.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
public class BusinessServiceCreateRequest {

    private UUID categoryId;
    private String categoryName;
    private UUID branchId;

    @NotBlank
    @Size(max = 255)
    private String name;

    @Size(max = 2000)
    private String description;

    private List<@Valid PurchaseDestinationRequest> purchaseDestinations;

    @NotNull
    private ServiceMode serviceMode;

    private BigDecimal basePrice;

    private String scheduleText;

    private Boolean isActive;

    private Map<String, Object> attributes;
}

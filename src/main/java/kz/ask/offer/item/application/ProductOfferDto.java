package kz.ask.offer.item.application;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import kz.ask.offer.item.domain.enums.ProductModerationStatus;
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
public class ProductOfferDto {

    private UUID productId;
    private UUID businessId;
    private UUID branchId;
    private UUID categoryId;
    private String categoryLabel;
    private String name;
    private String description;
    private List<String> imageFiles;
    private List<PurchaseDestinationDto> purchaseDestinations;
    private List<String> tags;
    private Map<String, Object> attributes;
    private BigDecimal price;
    private Boolean isActive;
    private ProductModerationStatus moderationStatus;
    private Instant updatedAt;
}

package kz.ask.catalog.application;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
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
    private UUID productOfferId;
    private Long searchVersion;
    private UUID businessId;
    private UUID branchId;
    private UUID categoryId;
    private String categoryLabel;
    private String name;
    private String description;
    private String sku;
    private List<String> tags;
    private BigDecimal price;
    private Boolean enabled;
    private String status;
    private String imageUrl;
    private Instant updatedAt;
}

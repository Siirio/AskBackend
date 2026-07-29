package kz.ask.platform.api.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PlatformCatalogEntryResponse {

    private UUID id;
    private String type;
    private String name;
    private UUID businessId;
    private String businessName;
    private String categoryLabel;
    private BigDecimal price;
    private String status;
    private Boolean isActive;
    private Integer discountPercent;
    private BigDecimal discountAmount;
    private Instant startsAt;
    private Instant endsAt;
    private Instant createdAt;
}

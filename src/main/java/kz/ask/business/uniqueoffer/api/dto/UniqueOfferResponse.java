package kz.ask.business.uniqueoffer.api.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UniqueOfferResponse {
    private UUID id;
    private UUID businessId;
    private String name;
    private String description;
    private Instant startDate;
    private Instant endDate;
    private String type;
    private String status;
    private String coverUrl;
    private Integer discountPercent;
    private java.math.BigDecimal discountAmount;
    private Boolean isActive;
    private String currency;
    private List<String> tags;
    private List<UUID> itemIds;
    private List<UUID> serviceIds;
    private List<UUID> branchIds;
}

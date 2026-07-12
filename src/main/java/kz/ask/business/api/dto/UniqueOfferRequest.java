package kz.ask.business.api.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UniqueOfferRequest {
    private String name;
    private String description;
    private Instant startDate;
    private Instant endDate;
    private String type;
    private String status;
    private String coverUrl;
    private Integer discountPercent;
    private java.math.BigDecimal discountAmount;
    private Boolean enabled;
    private List<String> tags;
    private List<UUID> productIds;
    private List<UUID> serviceIds;
    private List<UUID> branchIds;
}

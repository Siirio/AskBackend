package kz.ask.search.basic.domain;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SearchableServiceSource {

    private UUID serviceOfferingId;
    private UUID businessId;
    private UUID branchId;
    private String name;
    private String description;
    private String categoryLabel;
    private String businessName;
    private String branchName;
    private BigDecimal basePrice;
    private String scheduleText;
    private Map<String, Object> attributes;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private boolean active;
}

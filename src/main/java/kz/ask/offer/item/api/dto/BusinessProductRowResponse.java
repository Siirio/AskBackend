package kz.ask.offer.item.api.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
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
public class BusinessProductRowResponse {

    private UUID productId;
    private UUID branchId;
    private UUID categoryId;
    private String categoryLabel;
    private String name;
    private String description;
    private String deepLink;
    private List<String> tags;
    private Map<String, Object> attributes;
    private BigDecimal price;
    private Boolean isActive;
    private Instant updatedAt;
}

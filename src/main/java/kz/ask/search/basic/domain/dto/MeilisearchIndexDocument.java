package kz.ask.search.basic.domain.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MeilisearchIndexDocument {
    private String id;
    private String aggregateId;
    private String title;
    private String normalizedTitle;
    private String summary;
    private String embeddingText;
    private String brand;
    private String categoryPath;
    private String categoryLabel;
    private String businessName;
    private String branchName;
    private UUID businessId;
    private UUID branchId;
    private List<String> tokens;
    private BigDecimal price;
    private String currency;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String city;
    private String country;
    private String documentType;
    private Map<String, Object> verifiedAttributes;
    private String availabilityStatus;
    private Long projectionVersion;
    private Instant syncedAt;
}

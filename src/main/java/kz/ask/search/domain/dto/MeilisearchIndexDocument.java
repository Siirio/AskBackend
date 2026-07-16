package kz.ask.search.domain.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MeilisearchIndexDocument {
    private String id;
    private String aggregateId;
    private Long documentVersion;
    private String title;
    private String normalizedTitle;
    private String summary;
    private String aiSearchSummary;
    private String aliases;
    private String brand;
    private String categoryPath;
    private String categoryLabel;
    private String sku;
    private String characteristicsJson;
    private String businessName;
    private String branchName;
    private List<String> tokens;
    private BigDecimal price;
    private String currency;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String city;
    private String documentType;
    private Map<String, Object> verifiedAttributes;
    private Map<String, Object> aiAttributes;
    private Instant syncedAt;
}

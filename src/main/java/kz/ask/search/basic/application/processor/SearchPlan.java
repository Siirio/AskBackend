package kz.ask.search.basic.application.processor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import kz.ask.search.basic.domain.enums.SearchDocumentType;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(toBuilder = true)
public class SearchPlan {

    private String rawQuery;
    private SearchDocumentType itemType;
    private String city;
    private String country;
    private String userSelectedCategory;
    private String sort;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private Integer radiusMeters;
    private List<UUID> businessIds;
    private Double mapNorth;
    private Double mapSouth;
    private Double mapEast;
    private Double mapWest;
    private List<UUID> activeOfferAggregateIds;
    private Double userLatitude;
    private Double userLongitude;
    private BigDecimal inferredMinPrice;
    private BigDecimal inferredMaxPrice;
    private String inferredCity;
    private String ambiguity;
    private List<String> clarificationSuggestions;
}

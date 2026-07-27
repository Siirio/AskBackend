package kz.ask.search.basic.domain.dto;

import java.math.BigDecimal;
import java.util.List;
import kz.ask.search.basic.domain.enums.SearchDocumentType;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SearchFallbackQueryDto {

    private List<SearchDocumentType> documentTypes;
    private String query;
    private String category;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private String city;
    private String country;
    private Integer radiusMeters;
    private Double userLatitude;
    private Double userLongitude;
    private Integer candidateLimit;
    private Boolean openNow;
}

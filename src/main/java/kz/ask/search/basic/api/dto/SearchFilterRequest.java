package kz.ask.search.basic.api.dto;

import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SearchFilterRequest {
    private String category;
    private String city;
    private String country;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private Boolean openNow;
    private Integer radiusMeters;
}

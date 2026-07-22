package kz.ask.search.basic.api.dto;

import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SearchFilterRequest {
    private String scope;
    private String category;
    private String city;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
}

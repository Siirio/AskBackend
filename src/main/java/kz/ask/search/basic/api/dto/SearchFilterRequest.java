package kz.ask.search.basic.api.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SearchFilterRequest {

    @Size(max = 255)
    private String category;

    @Size(max = 255)
    private String city;

    @Size(max = 2)
    private String country;

    @DecimalMin(value = "0", inclusive = true)
    private BigDecimal minPrice;

    @DecimalMin(value = "0", inclusive = true)
    private BigDecimal maxPrice;

    private Boolean openNow;

    @Min(1)
    @Max(100000)
    private Integer radiusMeters;

    @AssertTrue
    public Boolean isPriceRangeValid() {
        return minPrice == null || maxPrice == null || minPrice.compareTo(maxPrice) <= 0;
    }
}

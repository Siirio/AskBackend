package kz.ask.search.basic.api.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import jakarta.validation.Valid;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SearchFilterRequest {

    @Size(max = 255)
    private String category;

    @Size(max = 255)
    private String city;

    @Size(min = 2, max = 2)
    private String country;

    @DecimalMin(value = "0", inclusive = true)
    private BigDecimal minPrice;

    @DecimalMin(value = "0", inclusive = true)
    private BigDecimal maxPrice;

    @Min(1)
    @Max(100000)
    private Integer radiusMeters;

    @Size(max = 100)
    private List<UUID> businessIds;

    @Valid
    private SearchMapAreaRequest mapArea;

    @AssertTrue
    public Boolean isPriceRangeValid() {
        return minPrice == null || maxPrice == null || minPrice.compareTo(maxPrice) <= 0;
    }

    @AssertTrue
    public Boolean isLocationFilterValid() {
        int selected = 0;
        if (city != null && !city.isBlank()) selected++;
        if (radiusMeters != null) selected++;
        if (mapArea != null) selected++;
        return selected <= 1;
    }

    @AssertTrue
    public Boolean isMapAreaValid() {
        return mapArea == null || mapArea.getNorth() == null || mapArea.getSouth() == null
                || mapArea.getEast() == null || mapArea.getWest() == null
                || mapArea.getNorth() > mapArea.getSouth()
                && mapArea.getEast() > mapArea.getWest();
    }
}

package kz.ask.search.api.dto;

import java.math.BigDecimal;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SearchIntentStructureRequest {

    @NotBlank
    private String rawQuery;

    private String selectedMode;
    private String selectedCategory;
    private String city;
    private String sort;

    @Valid
    private SearchLocationRequest userLocation;

    private String language;
    private BigDecimal explicitMinPrice;
    private BigDecimal explicitMaxPrice;
}

package kz.ask.search.basic.api.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.AssertTrue;
import kz.ask.search.basic.domain.enums.SearchScope;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SearchRequest {

    @NotBlank
    @Size(max = 500)
    private String rawQuery;

    @NotNull
    private SearchScope mode;

    @Pattern(regexp = "(?i)relevance|distance|price_asc|lowest_price")
    private String sort;

    @Valid
    private SearchLocationRequest userLocation;

    @Size(max = 16)
    @Pattern(regexp = "^[A-Za-z]{2}([_-][A-Za-z]{2})?$")
    private String locale;

    @Min(0)
    @Max(20)
    private Integer page;

    @Min(1)
    @Max(50)
    private Integer pageSize;

    @Valid
    private SearchFilterRequest explicitFilters;

    @AssertTrue
    public Boolean isRadiusLocationValid() {
        return explicitFilters == null || explicitFilters.getRadiusMeters() == null || userLocation != null;
    }
}

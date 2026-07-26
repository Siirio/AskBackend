package kz.ask.search.basic.api.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import kz.ask.search.basic.domain.enums.SearchScope;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SearchRequest {

    @NotBlank
    private String rawQuery;

    @NotNull
    private SearchScope mode;

    private String sort;

    @Valid
    private SearchLocationRequest userLocation;

    private String locale;

    @Min(0)
    @Max(20)
    private Integer page;

    @Min(1)
    @Max(50)
    private Integer pageSize;

    @Valid
    private SearchFilterRequest explicitFilters;
}

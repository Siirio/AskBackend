package kz.ask.search.basic.api.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SearchV2Request {

    @NotBlank
    private String rawQuery;

    private String scope;
    private String selectedCategory;
    private String city;
    private String sort;

    @Valid
    private SearchLocationRequest userLocation;

    private String language;

    @Min(0)
    @Max(20)
    private Integer page;

    @Min(1)
    @Max(50)
    private Integer pageSize;

    @Valid
    private SearchFilterRequest filters;

    @Valid
    private SearchOverrideRequest overrides;
}

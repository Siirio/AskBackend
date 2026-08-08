package kz.ask.search.decision.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import kz.ask.search.basic.api.dto.SearchFilterRequest;
import kz.ask.search.basic.api.dto.SearchLocationRequest;
import kz.ask.search.basic.domain.enums.SearchScope;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ClarificationRequest {

    @NotBlank
    @Size(max = 500)
    private String rawQuery;

    @NotNull
    private SearchScope mode;

    @Valid
    private SearchFilterRequest explicitFilters;

    @Valid
    private SearchLocationRequest userLocation;

    @Size(max = 16)
    @Pattern(regexp = "^[A-Za-z]{2}([_-][A-Za-z]{2})?$")
    private String locale;
}

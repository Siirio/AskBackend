package kz.ask.search.basic.api.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SearchMapAreaRequest {

    @NotNull
    @DecimalMin("-90")
    @DecimalMax("90")
    private Double north;

    @NotNull
    @DecimalMin("-90")
    @DecimalMax("90")
    private Double south;

    @NotNull
    @DecimalMin("-180")
    @DecimalMax("180")
    private Double east;

    @NotNull
    @DecimalMin("-180")
    @DecimalMax("180")
    private Double west;
}

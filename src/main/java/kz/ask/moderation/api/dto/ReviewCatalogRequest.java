package kz.ask.moderation.api.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReviewCatalogRequest {

    @NotNull
    private Boolean approved;
}

package kz.ask.search.basic.api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DecisionUseCaseRequest {

    @NotBlank
    private String key;

    @NotBlank
    private String label;

    private String source;
}

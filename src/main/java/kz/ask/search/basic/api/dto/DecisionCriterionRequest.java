package kz.ask.search.basic.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DecisionCriterionRequest {

    @NotBlank
    private String key;

    @NotBlank
    private String label;

    @Pattern(regexp = "EQ|NE|GTE|LTE|BETWEEN|CONTAINS")
    private String operator;

    private List<String> values;

    private String unit;

    @Pattern(regexp = "QUERY|USER|FILTER|CUSTOM_TEXT")
    private String source;
}

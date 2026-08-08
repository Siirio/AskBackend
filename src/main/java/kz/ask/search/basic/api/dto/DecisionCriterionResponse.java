package kz.ask.search.basic.api.dto;

import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.Singular;

@Getter
@Builder
public class DecisionCriterionResponse {

    private String key;
    private String label;
    private String operator;
    @Singular
    private List<String> values;
    private String unit;
    private String source;
}

package kz.ask.search.basic.application.processor;

import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.Singular;

@Getter
@Builder
public class InterpretedCriterion {

    private String key;
    private String label;
    private String operator;
    @Singular
    private List<String> values;
    private String unit;
}

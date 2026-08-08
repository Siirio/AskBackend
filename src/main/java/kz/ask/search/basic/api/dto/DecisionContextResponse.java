package kz.ask.search.basic.api.dto;

import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.Singular;

@Getter
@Builder
public class DecisionContextResponse {

    @Singular
    private List<DecisionCriterionResponse> hardConstraints;

    @Singular
    private List<DecisionCriterionResponse> preferences;

    @Singular
    private List<DecisionUseCaseResponse> useCases;

    @Singular
    private List<DecisionCriterionResponse> exclusions;

    private String customText;
}

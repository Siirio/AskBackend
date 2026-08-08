package kz.ask.search.basic.application.decision;

import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.Singular;

@Getter
@Builder
public class DecisionContext {

    @Singular
    private List<DecisionCriterion> hardConstraints;

    @Singular
    private List<DecisionCriterion> preferences;

    @Singular
    private List<DecisionUseCase> useCases;

    @Singular
    private List<DecisionCriterion> exclusions;

    private String customText;
}

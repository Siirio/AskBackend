package kz.ask.search.basic.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DecisionContextRequest {

    @Valid
    private List<DecisionCriterionRequest> hardConstraints;

    @Valid
    private List<DecisionCriterionRequest> preferences;

    @Valid
    private List<DecisionUseCaseRequest> useCases;

    @Valid
    private List<DecisionCriterionRequest> exclusions;

    @Size(max = 500)
    private String customText;
}

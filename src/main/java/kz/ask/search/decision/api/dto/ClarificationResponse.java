package kz.ask.search.decision.api.dto;

import java.util.List;
import kz.ask.search.basic.api.dto.DecisionContextResponse;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ClarificationResponse {

    private String rawQuery;
    private String understoodQuery;
    private Boolean clarificationRequired;
    private List<ClarificationFieldResponse> fields;
    private DecisionContextResponse prefilledDecisionContext;
}

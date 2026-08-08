package kz.ask.search.decision.application;

import java.util.List;
import java.util.UUID;
import kz.ask.search.basic.application.decision.CriterionAssessment;
import kz.ask.search.basic.application.decision.CriterionEvidence;
import lombok.Builder;
import lombok.Getter;
import lombok.Singular;

@Getter
@Builder
public class CandidateEvaluation {

    private UUID resultId;
    private String decisionLabel;

    @Singular
    private List<CriterionAssessment> criterionAssessments;

    @Singular
    private List<String> advantages;

    @Singular
    private List<String> tradeoffs;

    @Singular
    private List<String> unknowns;

    @Singular
    private List<CriterionEvidence> comparisonFacts;
}

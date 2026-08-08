package kz.ask.search.basic.application.decision;

import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.Singular;

@Getter
@Builder
public class CriterionAssessment {

    private String criterionKey;
    private String label;
    private String status;
    private String displayValue;
    private String consequence;

    @Singular("evidenceItem")
    private List<CriterionEvidence> evidence;
}

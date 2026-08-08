package kz.ask.search.basic.api.dto;

import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.Singular;

@Getter
@Builder
public class CriterionAssessmentResponse {

    private String criterionKey;
    private String label;
    private String status;
    private String displayValue;
    private String consequence;

    @Singular("evidenceItem")
    private List<CriterionEvidenceResponse> evidence;
}

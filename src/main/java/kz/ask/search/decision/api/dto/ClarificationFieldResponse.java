package kz.ask.search.decision.api.dto;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ClarificationFieldResponse {

    private String id;
    private String criterionKey;
    private String label;
    private String type;
    private Boolean required;
    private List<String> options;
    private Double min;
    private Double max;
    private String unit;
}

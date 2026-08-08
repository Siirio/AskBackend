package kz.ask.search.decision.api.dto;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CompareRowResponse {

    private String key;
    private String label;
    private Boolean isDifferent;
    private List<CompareValueResponse> values;
}

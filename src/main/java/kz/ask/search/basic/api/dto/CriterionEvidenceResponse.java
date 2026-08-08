package kz.ask.search.basic.api.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CriterionEvidenceResponse {

    private String source;
    private String key;
    private String value;
}

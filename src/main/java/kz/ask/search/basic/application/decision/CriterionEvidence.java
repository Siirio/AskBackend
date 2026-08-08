package kz.ask.search.basic.application.decision;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CriterionEvidence {

    private String source;
    private String key;
    private String value;
}

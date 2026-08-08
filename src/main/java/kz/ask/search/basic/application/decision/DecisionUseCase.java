package kz.ask.search.basic.application.decision;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DecisionUseCase {

    private String key;
    private String label;
    private String source;
}

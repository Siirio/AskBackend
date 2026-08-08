package kz.ask.search.basic.api.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DecisionUseCaseResponse {

    private String key;
    private String label;
    private String source;
}

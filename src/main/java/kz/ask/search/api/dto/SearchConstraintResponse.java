package kz.ask.search.api.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SearchConstraintResponse {
    private String key;
    private String value;
    private String source;
}

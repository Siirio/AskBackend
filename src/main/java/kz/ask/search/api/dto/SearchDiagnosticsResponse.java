package kz.ask.search.api.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SearchDiagnosticsResponse {
    private String engine;
    private Boolean fallbackUsed;
    private String fallbackReason;
    private Integer candidateCount;
    private Long latencyMs;
}

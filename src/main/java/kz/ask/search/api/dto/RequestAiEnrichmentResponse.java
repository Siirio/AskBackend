package kz.ask.search.api.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RequestAiEnrichmentResponse {

    private int queuedCount;
}

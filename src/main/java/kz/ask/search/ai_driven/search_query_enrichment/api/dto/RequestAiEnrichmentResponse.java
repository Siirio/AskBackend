package kz.ask.search.ai_driven.search_query_enrichment.api.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RequestAiEnrichmentResponse {

    private int queuedCount;
}

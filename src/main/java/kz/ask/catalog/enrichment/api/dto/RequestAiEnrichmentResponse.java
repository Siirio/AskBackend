package kz.ask.catalog.enrichment.api.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RequestAiEnrichmentResponse {

    private int enrichedCount;
}

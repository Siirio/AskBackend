package kz.ask.search.ai_driven.search_query_enrichment.domain;

import kz.ask.search.ai_driven.search_query_enrichment.domain.dto.SearchAiEnrichmentItem;
import kz.ask.search.ai_driven.search_query_enrichment.infrastructure.client.DeepSeekAttributeExtractor;

public interface SearchAiEnrichmentService {

    void complete(SearchAiEnrichmentItem item,
                  DeepSeekAttributeExtractor.ExtractionResult result);
}

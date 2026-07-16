package kz.ask.search.domain;

import kz.ask.search.domain.dto.SearchAiEnrichmentItem;
import kz.ask.search.infrastructure.client.DeepSeekAttributeExtractor;

public interface SearchAiEnrichmentService {

    void complete(SearchAiEnrichmentItem item,
                  DeepSeekAttributeExtractor.ExtractionResult result);
}

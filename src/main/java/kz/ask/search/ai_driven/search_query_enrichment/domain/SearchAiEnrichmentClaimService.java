package kz.ask.search.ai_driven.search_query_enrichment.domain;

import java.util.List;
import kz.ask.search.ai_driven.search_query_enrichment.domain.dto.SearchAiEnrichmentItem;

public interface SearchAiEnrichmentClaimService {

    List<SearchAiEnrichmentItem> claim(Integer batchSize, String workerId);

    void retry(SearchAiEnrichmentItem item, RuntimeException failure);
}

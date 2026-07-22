package kz.ask.search.ai_driven.search_query_enrichment.domain;

import com.fasterxml.jackson.databind.JsonNode;
import kz.ask.search.ai_driven.search_query_enrichment.api.dto.SearchIntentStructureRequest;

public interface SearchIntentStructurer {

    JsonNode structure(SearchIntentStructureRequest request);
}

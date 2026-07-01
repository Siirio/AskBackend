package kz.ask.search.domain;

import com.fasterxml.jackson.databind.JsonNode;
import kz.ask.search.api.dto.SearchIntentStructureRequest;

public interface SearchIntentStructurer {

    JsonNode structure(SearchIntentStructureRequest request);
}

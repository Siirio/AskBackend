package kz.ask.search.search_query_enrichment.domain;

import kz.ask.search.basic.application.processor.SearchInterpretation;
import kz.ask.search.search_query_enrichment.api.dto.SearchIntentStructureRequest;

public interface SearchIntentStructurer {

    SearchInterpretation interpret(SearchIntentStructureRequest request);
}

package kz.ask.search.ai_driven.search_query_enrichment.infrastructure.client;

import java.util.List;
import java.util.UUID;

class ExtractionResultRaw {

    UUID id;
    String searchSummary;
    List<String> aliases;
    List<ExtractionFactRaw> facts;
}

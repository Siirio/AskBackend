package kz.ask.search.ai_driven.search_query_enrichment.infrastructure.client;

import com.fasterxml.jackson.databind.JsonNode;
import java.math.BigDecimal;

class ExtractionFactRaw {

    String key;
    JsonNode value;
    BigDecimal confidence;
    String evidence;
}

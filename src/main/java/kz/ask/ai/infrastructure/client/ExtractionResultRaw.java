package kz.ask.ai.infrastructure.client;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

class ExtractionResultRaw {

    UUID id;
    String searchSummary;
    List<String> aliases;
    List<String> conceptIds;
    List<String> useCases;
    BigDecimal confidence;
    List<String> evidence;
    List<ExtractionFactRaw> facts;
}

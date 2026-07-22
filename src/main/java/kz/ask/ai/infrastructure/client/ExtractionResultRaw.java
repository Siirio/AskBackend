package kz.ask.ai.infrastructure.client;

import java.util.List;
import java.util.UUID;

class ExtractionResultRaw {

    UUID id;
    String searchSummary;
    List<String> aliases;
    List<ExtractionFactRaw> facts;
}

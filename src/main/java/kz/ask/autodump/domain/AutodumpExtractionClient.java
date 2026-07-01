package kz.ask.autodump.domain;

import com.fasterxml.jackson.databind.JsonNode;

public interface AutodumpExtractionClient {

    JsonNode extract(String rawText);
}

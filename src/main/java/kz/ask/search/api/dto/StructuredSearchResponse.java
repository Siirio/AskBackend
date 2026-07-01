package kz.ask.search.api.dto;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class StructuredSearchResponse {

    private JsonNode intentStructure;
    private String effectiveScope;
    private List<String> effectiveQueries;
    private List<SearchResultSectionResponse> sections;
    private List<SearchResultCardResponse> results;
}

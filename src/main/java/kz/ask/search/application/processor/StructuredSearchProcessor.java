package kz.ask.search.application.processor;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import kz.ask.search.api.dto.SearchIntentStructureRequest;
import kz.ask.search.api.dto.SearchResultSectionResponse;
import kz.ask.search.api.dto.SearchResultCardResponse;
import kz.ask.search.api.dto.StructuredSearchResponse;
import kz.ask.search.domain.SearchIntentStructurer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StructuredSearchProcessor {

    private final SearchIntentStructurer searchIntentStructurer;
    private final PublicSearchProcessor publicSearchProcessor;

    public JsonNode structure(SearchIntentStructureRequest request) {
        return searchIntentStructurer.structure(request);
    }

    public StructuredSearchResponse search(SearchIntentStructureRequest request, Integer page, Integer size) {
        JsonNode intentStructure = searchIntentStructurer.structure(request);
        SearchPlan searchPlan = publicSearchProcessor.buildSearchPlan(intentStructure, request);
        List<SearchResultCardResponse> results = publicSearchProcessor.searchStructured(searchPlan, page, size);
        return StructuredSearchResponse.builder()
                .intentStructure(intentStructure)
                .effectiveScope(publicSearchProcessor.resolveStructuredScope(intentStructure))
                .effectiveQueries(publicSearchProcessor.resolveStructuredQueryTerms(searchPlan))
                .sections(groupSections(results))
                .results(results)
                .build();
    }

    private List<SearchResultSectionResponse> groupSections(List<SearchResultCardResponse> results) {
        Map<String, List<SearchResultCardResponse>> grouped = new LinkedHashMap<>();
        grouped.put("EXACT", filterSection(results, "EXACT"));
        grouped.put("OVER_BUDGET", filterSection(results, "OVER_BUDGET"));
        grouped.put("WRONG_CITY", filterSection(results, "WRONG_CITY"));
        grouped.put("SIMILAR", filterSection(results, "SIMILAR"));
        return grouped.entrySet().stream()
                .filter(entry -> !entry.getValue().isEmpty())
                .map(entry -> SearchResultSectionResponse.builder()
                        .type(entry.getKey())
                        .title(sectionTitle(entry.getKey()))
                        .items(entry.getValue())
                        .build())
                .toList();
    }

    private List<SearchResultCardResponse> filterSection(List<SearchResultCardResponse> results, String sectionType) {
        return results.stream()
                .filter(result -> sectionType.equals(result.getSectionType()))
                .toList();
    }

    private String sectionTitle(String sectionType) {
        if ("OVER_BUDGET".equals(sectionType)) {
            return "Похожие варианты дороже бюджета";
        }
        if ("WRONG_CITY".equals(sectionType)) {
            return "Похожие варианты в другом городе";
        }
        if ("SIMILAR".equals(sectionType)) {
            return "Похожие варианты";
        }
        return "Подходит под запрос";
    }
}

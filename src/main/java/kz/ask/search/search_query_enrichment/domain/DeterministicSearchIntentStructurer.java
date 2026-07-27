package kz.ask.search.search_query_enrichment.domain;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import kz.ask.search.search_query_enrichment.api.dto.SearchIntentStructureRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DeterministicSearchIntentStructurer {

    private final ObjectMapper objectMapper;
    private final SearchConceptOntology searchConceptOntology;

    public JsonNode structure(SearchIntentStructureRequest request) {
        ObjectNode root = objectMapper.createObjectNode();
        root.put("request_type", request.getSelectedMode() + "_SEARCH");

        ObjectNode semantic = root.putObject("semantic");
        semantic.put("semantic_query", searchConceptOntology.normalizeQuery(request.getRawQuery()));
        ArrayNode keywords = semantic.putArray("search_keywords");
        keywords.add(request.getRawQuery().trim());
        semantic.putArray("synonyms");
        ArrayNode relatedTerms = semantic.putArray("related_terms");
        searchConceptOntology.resolveExpansions(request.getRawQuery()).forEach(relatedTerms::add);
        ArrayNode concepts = semantic.putArray("concepts");
        searchConceptOntology.resolveConceptIds(request.getRawQuery()).forEach(conceptId -> {
            ObjectNode concept = concepts.addObject();
            concept.put("id", conceptId);
            concept.put("weight", 1.0);
        });
        semantic.put("ambiguity",
                searchConceptOntology.resolveExpansions(request.getRawQuery()).size() > 1 ? "HIGH" : "LOW");

        root.putObject("item");
        root.putObject("service");
        root.putObject("price");
        ObjectNode constraints = root.putObject("constraints");
        constraints.putArray("must_have");
        constraints.putArray("nice_to_have");
        constraints.putArray("not_wanted");
        ObjectNode ranking = root.putObject("ranking");
        ranking.putArray("prioritize");
        ranking.putArray("expand_if_no_results");
        return root;
    }
}

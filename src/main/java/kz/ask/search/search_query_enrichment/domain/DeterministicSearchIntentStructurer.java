package kz.ask.search.search_query_enrichment.domain;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.List;
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
        List<String> expansions = searchConceptOntology.resolveExpansions(request.getRawQuery());
        ArrayNode relatedTerms = semantic.putArray("related_terms");
        expansions.forEach(relatedTerms::add);
        ArrayNode concepts = semantic.putArray("concepts");
        searchConceptOntology.resolveConceptIds(request.getRawQuery()).forEach(conceptId -> {
            ObjectNode concept = concepts.addObject();
            concept.put("id", conceptId);
            concept.put("weight", 1.0);
        });
        semantic.put("ambiguity", expansions.size() > 1 ? "HIGH" : "LOW");

        ArrayNode hypotheses = root.putArray("intent_hypotheses");
        for (int index = 0; index < expansions.size(); index++) {
            ObjectNode hypothesis = hypotheses.addObject();
            hypothesis.put("intent_id", "deterministic-" + index);
            hypothesis.put("probability", 1.0 / expansions.size());
            ArrayNode terms = hypothesis.putArray("terms");
            ObjectNode term = terms.addObject();
            term.put("term", expansions.get(index));
            term.put("weight", 1.0);
            hypothesis.putArray("concepts");
        }

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
        ObjectNode clarification = root.putObject("clarification");
        clarification.put("needed", expansions.size() > 1);
        ArrayNode suggestions = clarification.putArray("suggestions");
        expansions.forEach(suggestions::add);
        return root;
    }
}

package kz.ask.search.search_query_enrichment.domain;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import kz.ask.search.search_query_enrichment.api.dto.SearchIntentStructureRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DeterministicSearchIntentStructurer {

    private static final Set<String> SERVICE_TERMS = Set.of(
            "service", "repair", "delivery", "cleaning", "rental", "rent",
            "услуга", "ремонт", "доставка", "уборка", "прокат", "аренда",
            "қызмет", "жөндеу", "жеткізу", "тазалау", "жалға");

    private static final Set<String> PRODUCT_TERMS = Set.of(
            "buy", "product", "goods", "shop", "price",
            "купить", "товар", "магазин", "цена",
            "сатып", "тауар", "дүкен", "бағасы");

    private final ObjectMapper objectMapper;

    public JsonNode structure(SearchIntentStructureRequest request) {
        ObjectNode root = objectMapper.createObjectNode();
        root.put("request_type", resolveRequestType(request));

        ObjectNode semantic = root.putObject("semantic");
        semantic.put("semantic_query", request.getRawQuery().trim());
        ArrayNode keywords = semantic.putArray("search_keywords");
        keywords.add(request.getRawQuery().trim());
        semantic.putArray("synonyms");
        semantic.putArray("related_terms");

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

    private String resolveRequestType(SearchIntentStructureRequest request) {
        if ("ITEM".equalsIgnoreCase(request.getSelectedMode()) || "PRODUCT".equalsIgnoreCase(request.getSelectedMode())) {
            return "ITEM_SEARCH";
        }
        if ("SERVICE".equalsIgnoreCase(request.getSelectedMode())) {
            return "SERVICE_SEARCH";
        }
        List<String> tokens = List.of(normalize(request.getRawQuery()).split("[^\\p{L}\\p{N}]+"));
        boolean service = tokens.stream().anyMatch(SERVICE_TERMS::contains);
        boolean product = tokens.stream().anyMatch(PRODUCT_TERMS::contains);
        if (service && !product) {
            return "SERVICE_SEARCH";
        }
        if (product && !service) {
            return "ITEM_SEARCH";
        }
        return "GENERAL_SEARCH";
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }
}

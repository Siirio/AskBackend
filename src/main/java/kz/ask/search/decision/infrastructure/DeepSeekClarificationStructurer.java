package kz.ask.search.decision.infrastructure;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import kz.ask.ai.infrastructure.client.BaseDeepSeekClient;
import kz.ask.search.basic.api.dto.DecisionContextResponse;
import kz.ask.search.basic.api.dto.DecisionCriterionResponse;
import kz.ask.search.basic.api.dto.DecisionUseCaseResponse;
import kz.ask.search.decision.api.dto.ClarificationFieldResponse;
import kz.ask.search.decision.api.dto.ClarificationResponse;
import kz.ask.shared.error.ErrorCode;
import kz.ask.shared.error.ExternalServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
@RequiredArgsConstructor
public class DeepSeekClarificationStructurer extends BaseDeepSeekClient {

    private final RestClient deepSeekRestClient;
    private final ObjectMapper objectMapper;

    @Value("${ask.ai.clarification.max-tokens:800}")
    private Integer maxTokens;

    @Value("classpath:prompts/decision-clarification.md")
    private Resource promptResource;

    public ClarificationResponse clarify(String rawQuery, String mode, String category,
                                          String city, String language) {
        if (!StringUtils.hasText(apiKey)) {
            return ClarificationResponse.builder()
                    .rawQuery(rawQuery)
                    .understoodQuery(rawQuery)
                    .clarificationRequired(false)
                    .fields(List.of())
                    .build();
        }
        try {
            JsonNode result = callDeepSeek(rawQuery, mode, category, city, language);
            return parseResponse(result, rawQuery);
        } catch (RestClientException | JsonProcessingException ex) {
            return ClarificationResponse.builder()
                    .rawQuery(rawQuery)
                    .understoodQuery(rawQuery)
                    .clarificationRequired(false)
                    .fields(List.of())
                    .build();
        }
    }

    private ClarificationResponse parseResponse(JsonNode result, String rawQuery) {
        String understoodQuery = result.path("understood_query").asText(rawQuery);
        boolean clarificationRequired = result.path("clarification_required").asBoolean(false);
        List<ClarificationFieldResponse> fields = parseFields(result.path("fields"));
        DecisionContextResponse prefilled = parsePrefilledContext(result.path("prefilled_decision_context"));

        return ClarificationResponse.builder()
                .rawQuery(rawQuery)
                .understoodQuery(understoodQuery)
                .clarificationRequired(clarificationRequired && !fields.isEmpty())
                .fields(fields)
                .prefilledDecisionContext(prefilled)
                .build();
    }

    private List<ClarificationFieldResponse> parseFields(JsonNode fieldsNode) {
        List<ClarificationFieldResponse> fields = new ArrayList<>();
        if (fieldsNode == null || !fieldsNode.isArray()) {
            return fields;
        }
        int count = 0;
        for (JsonNode node : fieldsNode) {
            if (count >= 4) {
                break;
            }
            String type = node.path("type").asText("SINGLE_SELECT");
            List<String> options = new ArrayList<>();
            JsonNode optionsNode = node.path("options");
            if (optionsNode.isArray()) {
                optionsNode.forEach(o -> {
                    String text = o.asText("");
                    if (!text.isBlank()) {
                        options.add(text);
                    }
                });
            }
            fields.add(ClarificationFieldResponse.builder()
                    .id(node.path("id").asText(""))
                    .criterionKey(node.path("criterion_key").asText(""))
                    .label(node.path("label").asText(""))
                    .type(type)
                    .required(node.path("required").asBoolean(false))
                    .options(options)
                    .min(node.has("min") && !node.path("min").isNull() ? node.path("min").asDouble() : null)
                    .max(node.has("max") && !node.path("max").isNull() ? node.path("max").asDouble() : null)
                    .unit(node.path("unit").asText(null))
                    .build());
            count++;
        }
        return fields;
    }

    private DecisionContextResponse parsePrefilledContext(JsonNode contextNode) {
        if (contextNode == null || contextNode.isMissingNode()) {
            return null;
        }
        List<DecisionCriterionResponse> hardConstraints = parseCriteria(contextNode.path("hard_constraints"), "QUERY");
        List<DecisionCriterionResponse> preferences = parseCriteria(contextNode.path("preferences"), "QUERY");
        List<DecisionCriterionResponse> exclusions = parseCriteria(contextNode.path("exclusions"), "QUERY");
        List<DecisionUseCaseResponse> useCases = new ArrayList<>();
        JsonNode useCasesNode = contextNode.path("use_cases");
        if (useCasesNode.isArray()) {
            useCasesNode.forEach(node -> useCases.add(DecisionUseCaseResponse.builder()
                    .key(node.path("key").asText(""))
                    .label(node.path("label").asText(""))
                    .source(node.path("source").asText("QUERY"))
                    .build()));
        }
        if (hardConstraints.isEmpty() && preferences.isEmpty()
                && exclusions.isEmpty() && useCases.isEmpty()) {
            return null;
        }
        return DecisionContextResponse.builder()
                .hardConstraints(hardConstraints)
                .preferences(preferences)
                .exclusions(exclusions)
                .useCases(useCases)
                .build();
    }

    private List<DecisionCriterionResponse> parseCriteria(JsonNode arrayNode, String source) {
        List<DecisionCriterionResponse> criteria = new ArrayList<>();
        if (arrayNode == null || !arrayNode.isArray()) {
            return criteria;
        }
        arrayNode.forEach(node -> {
            List<String> values = new ArrayList<>();
            JsonNode valuesNode = node.path("values");
            if (valuesNode.isArray()) {
                valuesNode.forEach(v -> {
                    String text = v.asText("");
                    if (!text.isBlank()) {
                        values.add(text);
                    }
                });
            }
            criteria.add(DecisionCriterionResponse.builder()
                    .key(node.path("key").asText(""))
                    .label(node.path("label").asText(""))
                    .operator(node.path("operator").asText("EQ"))
                    .values(values)
                    .unit(node.path("unit").asText(null))
                    .source(source)
                    .build());
        });
        return criteria;
    }

    private JsonNode callDeepSeek(String rawQuery, String mode, String category,
                                   String city, String language)
            throws JsonProcessingException {
        JsonNode response = deepSeekRestClient.post()
                .uri(CHAT_COMPLETIONS_PATH)
                .header("Authorization", "Bearer " + apiKey)
                .body(Map.of(
                        "model", model,
                        "messages", List.of(
                                Map.of("role", "system", "content", readPrompt(promptResource)),
                                Map.of("role", "user", "content", objectMapper.writeValueAsString(Map.of(
                                        "raw_query", rawQuery,
                                        "selected_mode", mode,
                                        "selected_category", category != null ? category : "",
                                        "city", city != null ? city : "",
                                        "language", language != null ? language : "ru"
                                )))
                        ),
                        "response_format", Map.of("type", "json_object"),
                        "stream", false,
                        "max_tokens", maxTokens
                ))
                .retrieve()
                .body(JsonNode.class);
        String outputText = response.path("choices").path(0).path("message").path("content").asText("");
        if (!StringUtils.hasText(outputText)) {
            throw new ExternalServiceException(ErrorCode.AI_INTENT_STRUCTURE_FAILED);
        }
        return objectMapper.readTree(outputText);
    }
}

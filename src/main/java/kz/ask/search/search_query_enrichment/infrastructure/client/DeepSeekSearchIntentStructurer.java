package kz.ask.search.search_query_enrichment.infrastructure.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import kz.ask.search.basic.application.processor.SearchInterpretation;
import kz.ask.search.search_query_enrichment.api.dto.SearchIntentStructureRequest;
import kz.ask.search.search_query_enrichment.infrastructure.cache.IntentStructureCache;
import kz.ask.shared.error.ErrorCode;
import kz.ask.shared.error.ExternalServiceException;
import kz.ask.shared.error.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
@RequiredArgsConstructor
public class DeepSeekSearchIntentStructurer {

    private static final String CHAT_COMPLETIONS_PATH = "/chat/completions";

    private final RestClient deepSeekRestClient;
    private final ObjectMapper objectMapper;
    private final IntentStructureCache cache;

    @Value("${ask.ai.search.api-key:}")
    private String apiKey;

    @Value("${ask.ai.search.model:deepseek-v4-flash}")
    private String model;

    @Value("${ask.ai.search.max-tokens:1800}")
    private Integer maxTokens;

    @Value("${ask.ai.search.prompt-version:1}")
    private String promptVersion;

    @Value("${ask.ai.search.schema-version:1}")
    private String schemaVersion;

    @Value("classpath:prompts/search-intent-structurer.md")
    private Resource promptResource;

    public SearchInterpretation interpret(SearchIntentStructureRequest request) {
        String cacheKey = request.getRawQuery() + "|" + request.getSelectedMode() + "|" + request.getLanguage();
        SearchInterpretation cached = cache.getInterpretation(cacheKey, promptVersion, model, schemaVersion);
        if (cached != null) {
            return cached;
        }
        if (!StringUtils.hasText(apiKey)) {
            throw new ValidationException(ErrorCode.AI_SEARCH_API_KEY_MISSING);
        }
        JsonNode result = callDeepSeek(request);
        SearchInterpretation interpretation = parseInterpretation(result, request);
        cache.putInterpretation(cacheKey, promptVersion, model, schemaVersion, interpretation);
        return interpretation;
    }

    public Boolean isAvailable() {
        return StringUtils.hasText(apiKey);
    }

    private SearchInterpretation parseInterpretation(JsonNode result, SearchIntentStructureRequest request) {
        JsonNode semantic = result.path("semantic");
        String normalizedQuery = semantic.path("semantic_query").asText(request.getRawQuery());
        JsonNode constraints = result.path("constraints");
        BigDecimal inferredMinPrice = parseDecimal(constraints.path("min_price").asText(null));
        BigDecimal inferredMaxPrice = parseDecimal(constraints.path("max_price").asText(null));
        String inferredCity = normalize(constraints.path("city").asText(null));
        String ambiguity = semantic.path("ambiguity").asText("LOW").trim().toUpperCase(Locale.ROOT);
        if (!List.of("LOW", "MEDIUM", "HIGH").contains(ambiguity)) {
            ambiguity = "LOW";
        }
        List<String> suggestions = new ArrayList<>();
        JsonNode suggestionNodes = result.path("clarification").path("suggestions");
        if (suggestionNodes.isArray()) {
            suggestionNodes.forEach(node -> {
                String text = normalize(node.asText(""));
                if (!text.isBlank() && suggestions.size() < 6) {
                    suggestions.add(text);
                }
            });
        }
        return SearchInterpretation.builder()
                .normalizedQuery(normalizedQuery)
                .inferredMinPrice(inferredMinPrice)
                .inferredMaxPrice(inferredMaxPrice)
                .inferredCity(inferredCity)
                .ambiguity(ambiguity)
                .suggestions(suggestions)
                .build();
    }

    private BigDecimal parseDecimal(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return new BigDecimal(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim().toLowerCase(Locale.ROOT);
    }

    private JsonNode callDeepSeek(SearchIntentStructureRequest request) {
        try {
            JsonNode response = deepSeekRestClient.post()
                    .uri(CHAT_COMPLETIONS_PATH)
                    .header("Authorization", "Bearer " + apiKey)
                    .body(buildPayload(request))
                    .retrieve()
                    .body(JsonNode.class);
            return readStructuredOutput(response);
        } catch (RestClientException | JsonProcessingException ex) {
            throw new ExternalServiceException(ErrorCode.AI_INTENT_STRUCTURE_FAILED);
        }
    }

    private Map<String, Object> buildPayload(SearchIntentStructureRequest request) throws JsonProcessingException {
        return Map.of(
                "model", model,
                "messages", List.of(
                        Map.of("role", "system", "content", readPrompt()),
                        Map.of("role", "user", "content", objectMapper.writeValueAsString(request))
                ),
                "response_format", Map.of("type", "json_object"),
                "thinking", Map.of("type", "disabled"),
                "stream", false,
                "max_tokens", maxTokens
        );
    }

    private String readPrompt() {
        try {
            return promptResource.getContentAsString(java.nio.charset.StandardCharsets.UTF_8);
        } catch (java.io.IOException ex) {
            throw new ExternalServiceException(ErrorCode.AI_INTENT_STRUCTURE_FAILED);
        }
    }

    private JsonNode readStructuredOutput(JsonNode response) throws JsonProcessingException {
        String outputText = response.path("choices").path(0).path("message").path("content").asText("");
        if (!StringUtils.hasText(outputText)) {
            throw new ExternalServiceException(ErrorCode.AI_INTENT_STRUCTURE_FAILED);
        }
        return objectMapper.readTree(outputText);
    }
}

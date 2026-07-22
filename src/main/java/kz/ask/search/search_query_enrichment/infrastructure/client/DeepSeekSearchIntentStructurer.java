package kz.ask.search.search_query_enrichment.infrastructure.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
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

    @Value("classpath:prompts/search-intent-structurer.md")
    private Resource promptResource;

    public JsonNode structure(SearchIntentStructureRequest request) {
        JsonNode cached = cache.get(request.getRawQuery());
        if (cached != null) {
            return cached;
        }
        if (!StringUtils.hasText(apiKey)) {
            throw new ValidationException(ErrorCode.AI_SEARCH_API_KEY_MISSING);
        }
        JsonNode result = callDeepSeek(request);
        cache.put(request.getRawQuery(), result);
        return result;
    }

    public Boolean isAvailable() {
        return StringUtils.hasText(apiKey);
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
        JsonNode structured = objectMapper.readTree(outputText);
        if (!StringUtils.hasText(structured.path("request_type").asText(""))) {
            throw new ExternalServiceException(ErrorCode.AI_INTENT_STRUCTURE_FAILED);
        }
        return structured;
    }
}

package kz.ask.search.infrastructure.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import kz.ask.shared.error.ErrorCode;
import kz.ask.shared.error.ExternalServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeepSeekAttributeExtractor {

    private static final String CHAT_COMPLETIONS_PATH = "/chat/completions";
    private static final int BATCH_SIZE = 20;

    private final RestClient deepSeekRestClient;
    private final ObjectMapper objectMapper;

    @Value("${ask.ai.search.api-key:}")
    private String apiKey;

    @Value("${ask.ai.search.model:deepseek-v4-flash}")
    private String model;

    @Value("classpath:prompts/attribute-extraction.md")
    private Resource promptResource;

    public record ExtractionItem(UUID id, String name, String description, String categoryLabel, List<String> tags) {}

    public record ExtractionResult(UUID id, Map<String, Object> attributes) {}

    public List<ExtractionResult> extractBatch(List<ExtractionItem> items) {
        if (!StringUtils.hasText(apiKey)) {
            log.warn("DeepSeek API key not configured, skipping attribute extraction for {} items", items.size());
            return Collections.emptyList();
        }
        if (items.isEmpty()) {
            return Collections.emptyList();
        }

        List<List<ExtractionItem>> batches = partition(items, BATCH_SIZE);
        return batches.stream()
                .flatMap(batch -> extractSingleBatch(batch).stream())
                .toList();
    }

    private List<ExtractionResult> extractSingleBatch(List<ExtractionItem> batch) {
        try {
            JsonNode response = deepSeekRestClient.post()
                    .uri(CHAT_COMPLETIONS_PATH)
                    .header("Authorization", "Bearer " + apiKey)
                    .body(buildPayload(batch))
                    .retrieve()
                    .body(JsonNode.class);
            return parseResults(response);
        } catch (RestClientException | JsonProcessingException ex) {
            log.error("Attribute extraction batch failed: {}", ex.getMessage());
            throw new ExternalServiceException(ErrorCode.AI_INTENT_STRUCTURE_FAILED);
        }
    }

    private Map<String, Object> buildPayload(List<ExtractionItem> items) throws JsonProcessingException {
        List<Map<String, Object>> inputItems = items.stream()
                .map(item -> Map.<String, Object>of(
                        "id", item.id().toString(),
                        "name", item.name() != null ? item.name() : "",
                        "description", item.description() != null ? item.description() : "",
                        "categoryLabel", item.categoryLabel() != null ? item.categoryLabel() : "",
                        "tags", item.tags() != null ? item.tags() : List.of()))
                .toList();

        return Map.of(
                "model", model,
                "messages", List.of(
                        Map.of("role", "system", "content", readPrompt()),
                        Map.of("role", "user", "content", objectMapper.writeValueAsString(inputItems))
                ),
                "response_format", Map.of("type", "json_object"),
                "thinking", Map.of("type", "disabled"),
                "stream", false,
                "max_tokens", 4000
        );
    }

    private String readPrompt() {
        try {
            return promptResource.getContentAsString(java.nio.charset.StandardCharsets.UTF_8);
        } catch (java.io.IOException ex) {
            throw new ExternalServiceException(ErrorCode.AI_INTENT_STRUCTURE_FAILED);
        }
    }

    private List<ExtractionResult> parseResults(JsonNode response) throws JsonProcessingException {
        String outputText = response.path("choices").path(0).path("message").path("content").asText("");
        if (!StringUtils.hasText(outputText)) {
            return Collections.emptyList();
        }
        JsonNode contentNode = objectMapper.readTree(outputText);
        JavaType listType = objectMapper.getTypeFactory()
                .constructCollectionType(List.class, ExtractionResultRaw.class);
        List<ExtractionResultRaw> raw = objectMapper.convertValue(contentNode, listType);
        if (raw == null) {
            return Collections.emptyList();
        }
        return raw.stream()
                .filter(r -> r.id != null && r.attributes != null && !r.attributes.isEmpty())
                .map(r -> new ExtractionResult(r.id, r.attributes))
                .toList();
    }

    private List<List<ExtractionItem>> partition(List<ExtractionItem> items, int size) {
        if (items.size() <= size) {
            return List.of(items);
        }
        return java.util.stream.IntStream.range(0, (items.size() + size - 1) / size)
                .mapToObj(i -> items.subList(i * size, Math.min((i + 1) * size, items.size())))
                .toList();
    }

}

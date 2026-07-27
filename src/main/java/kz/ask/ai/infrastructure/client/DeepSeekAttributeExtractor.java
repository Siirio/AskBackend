package kz.ask.ai.infrastructure.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import kz.ask.search.basic.domain.AttributeKeys;
import kz.ask.shared.error.ErrorCode;
import kz.ask.shared.error.ExternalServiceException;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
@RequiredArgsConstructor
public class DeepSeekAttributeExtractor {

    private static final String CHAT_COMPLETIONS_PATH = "/chat/completions";
    private static final Integer REQUEST_BATCH_SIZE = 20;

    private final RestClient deepSeekRestClient;
    private final ObjectMapper objectMapper;

    @Value("${ask.ai.search.api-key:}")
    private String apiKey;

    @Value("${ask.ai.search.model:deepseek-v4-flash}")
    private String model;

    @Value("${ask.search.ai-enrichment.max-tokens:4000}")
    private Integer maxTokens;

    @Value("classpath:prompts/attribute-extraction.md")
    private Resource promptResource;

    public Boolean isAvailable() {
        return StringUtils.hasText(apiKey);
    }

    public String modelVersion() {
        return model;
    }

    public List<ExtractionResult> extractBatch(List<ExtractionItem> items) {
        if (!isAvailable() || items.isEmpty()) {
            return Collections.emptyList();
        }
        return partition(items, REQUEST_BATCH_SIZE).stream()
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
        } catch (RestClientException | JsonProcessingException failure) {
            throw new ExternalServiceException(ErrorCode.AI_INTENT_STRUCTURE_FAILED);
        }
    }

    private Map<String, Object> buildPayload(List<ExtractionItem> items) throws JsonProcessingException {
        List<Map<String, Object>> inputItems = items.stream()
                .map(item -> Map.<String, Object>of(
                        "id", item.getId().toString(),
                        "title", value(item.getTitle()),
                        "description", value(item.getDescription()),
                        "categoryLabel", value(item.getCategoryLabel()),
                        "aliases", value(item.getAliases())))
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
                "max_tokens", maxTokens
        );
    }

    private String readPrompt() {
        try {
            return promptResource.getContentAsString(java.nio.charset.StandardCharsets.UTF_8);
        } catch (java.io.IOException failure) {
            throw new ExternalServiceException(ErrorCode.AI_INTENT_STRUCTURE_FAILED);
        }
    }

    private List<ExtractionResult> parseResults(JsonNode response) throws JsonProcessingException {
        String outputText = response.path("choices").path(0).path("message").path("content").asText("");
        if (!StringUtils.hasText(outputText)) {
            return Collections.emptyList();
        }
        JsonNode root = objectMapper.readTree(outputText);
        JsonNode items = root.isArray() ? root : root.path("items");
        if (!items.isArray()) {
            return Collections.emptyList();
        }
        JavaType listType = objectMapper.getTypeFactory()
                .constructCollectionType(List.class, ExtractionResultRaw.class);
        List<ExtractionResultRaw> rawResults = objectMapper.convertValue(items, listType);
        return rawResults.stream()
                .filter(raw -> raw.id != null)
                .map(this::toResult)
                .toList();
    }

    private ExtractionResult toResult(ExtractionResultRaw raw) {
        List<ExtractionFact> facts = raw.facts == null
                ? List.of()
                : raw.facts.stream()
                        .filter(this::validFact)
                        .map(rawFact -> ExtractionFact.builder()
                                .key(rawFact.key)
                                .value(rawFact.value)
                                .confidence(rawFact.confidence)
                                .evidence(rawFact.evidence.trim())
                                .build())
                        .toList();
        return ExtractionResult.builder()
                .id(raw.id)
                .searchSummary(raw.searchSummary)
                .aliases(raw.aliases == null ? List.of() : raw.aliases.stream()
                        .filter(StringUtils::hasText)
                        .map(String::trim)
                        .distinct()
                        .toList())
                .conceptIds(raw.conceptIds == null ? List.of() : raw.conceptIds.stream()
                        .filter(StringUtils::hasText)
                        .map(String::trim)
                        .distinct()
                        .toList())
                .useCases(raw.useCases == null ? List.of() : raw.useCases.stream()
                        .filter(StringUtils::hasText)
                        .map(String::trim)
                        .distinct()
                        .toList())
                .confidence(validConfidence(raw.confidence) ? raw.confidence : null)
                .evidence(raw.evidence == null ? List.of() : raw.evidence.stream()
                        .filter(StringUtils::hasText)
                        .map(String::trim)
                        .distinct()
                        .toList())
                .facts(facts)
                .build();
    }

    private boolean validConfidence(BigDecimal confidence) {
        return confidence != null
                && confidence.compareTo(BigDecimal.ZERO) >= 0
                && confidence.compareTo(BigDecimal.ONE) <= 0;
    }

    private boolean validFact(ExtractionFactRaw fact) {
        return fact != null
                && AttributeKeys.ALL_KEYS.contains(fact.key)
                && fact.value != null
                && !fact.value.isNull()
                && fact.confidence != null
                && fact.confidence.compareTo(BigDecimal.ZERO) >= 0
                && fact.confidence.compareTo(BigDecimal.ONE) <= 0
                && StringUtils.hasText(fact.evidence);
    }

    private List<List<ExtractionItem>> partition(List<ExtractionItem> items, Integer size) {
        if (items.size() <= size) {
            return List.of(items);
        }
        return java.util.stream.IntStream.range(0, (items.size() + size - 1) / size)
                .mapToObj(index -> items.subList(index * size, Math.min((index + 1) * size, items.size())))
                .toList();
    }

    private String value(String input) {
        return input == null ? "" : input;
    }

    @Getter
    @Builder
    public static class ExtractionItem {
        private UUID id;
        private String title;
        private String description;
        private String categoryLabel;
        private String aliases;
    }

    @Getter
    @Builder
    public static class ExtractionFact {
        private String key;
        private JsonNode value;
        private BigDecimal confidence;
        private String evidence;
    }

    @Getter
    @Builder
    public static class ExtractionResult {
        private UUID id;
        private String searchSummary;
        private List<String> aliases;
        private List<String> conceptIds;
        private List<String> useCases;
        private BigDecimal confidence;
        private List<String> evidence;
        private List<ExtractionFact> facts;
    }
}

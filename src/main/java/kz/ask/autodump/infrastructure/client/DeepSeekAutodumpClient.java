package kz.ask.autodump.infrastructure.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import kz.ask.autodump.domain.AutodumpExtractionClient;
import kz.ask.shared.error.ErrorCode;
import kz.ask.shared.error.ExternalServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
@RequiredArgsConstructor
public class DeepSeekAutodumpClient implements AutodumpExtractionClient {

    private final RestClient autodumpRestClient;
    private final ObjectMapper objectMapper;

    @Value("${ask.ai.autodump.api-key:}")
    private String apiKey;

    @Value("${ask.ai.autodump.model:deepseek-v4-flash}")
    private String model;

    @Value("${ask.ai.autodump.max-tokens:4000}")
    private int maxTokens;

    @Value("classpath:prompts/autodump-extraction.md")
    private Resource promptResource;

    @Override
    public JsonNode extract(String rawText) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new ExternalServiceException(ErrorCode.AI_SEARCH_API_KEY_MISSING);
        }
        String systemPrompt = loadPrompt();
        return callDeepSeek(systemPrompt, rawText);
    }

    private String loadPrompt() {
        try {
            return promptResource.getContentAsString(StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new ExternalServiceException(ErrorCode.INTERNAL_ERROR);
        }
    }

    private JsonNode callDeepSeek(String systemPrompt, String userContent) {
        try {
            Map<String, Object> requestBody = Map.of(
                    "model", model,
                    "messages", new Object[]{
                            Map.of("role", "system", "content", systemPrompt),
                            Map.of("role", "user", "content", userContent)
                    },
                    "response_format", Map.of("type", "json_object"),
                    "thinking", Map.of("type", "disabled"),
                    "stream", false,
                    "max_tokens", maxTokens
            );

            String response = autodumpRestClient.post()
                    .uri("/chat/completions")
                    .header("Authorization", "Bearer " + apiKey)
                    .body(requestBody)
                    .retrieve()
                    .body(String.class);

            JsonNode root = objectMapper.readTree(response);
            JsonNode content = root.path("choices").path(0).path("message").path("content");
            if (content.isMissingNode()) {
                throw new ExternalServiceException(ErrorCode.AI_INTENT_STRUCTURE_FAILED);
            }
            return objectMapper.readTree(content.asText());
        } catch (RestClientException | JsonProcessingException e) {
            throw new ExternalServiceException(ErrorCode.AI_INTENT_STRUCTURE_FAILED);
        }
    }
}

package kz.ask.search.domain;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import kz.ask.search.api.dto.SearchIntentStructureRequest;
import kz.ask.search.infrastructure.client.DeepSeekSearchIntentStructurer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class LayeredSearchIntentStructurer implements SearchIntentStructurer {

    private final DeterministicSearchIntentStructurer deterministicStructurer;
    private final DeepSeekSearchIntentStructurer aiStructurer;

    @Override
    public JsonNode structure(SearchIntentStructureRequest request) {
        JsonNode deterministic = deterministicStructurer.structure(request);
        if (!aiStructurer.isAvailable()) {
            return deterministic;
        }
        try {
            JsonNode enhanced = aiStructurer.structure(request);
            enforceExplicitMode(enhanced, request);
            return enhanced;
        } catch (RuntimeException failure) {
            log.warn("AI query enhancement is unavailable: {}. Using deterministic interpretation.",
                    rootCauseMessage(failure));
            return deterministic;
        }
    }

    private void enforceExplicitMode(JsonNode enhanced, SearchIntentStructureRequest request) {
        if (!(enhanced instanceof ObjectNode objectNode)) {
            return;
        }
        if ("PRODUCT".equalsIgnoreCase(request.getSelectedMode())) {
            objectNode.put("request_type", "PRODUCT_SEARCH");
        } else if ("SERVICE".equalsIgnoreCase(request.getSelectedMode())) {
            objectNode.put("request_type", "SERVICE_SEARCH");
        }
    }

    private String rootCauseMessage(Throwable failure) {
        Throwable rootCause = failure;
        while (rootCause.getCause() != null) {
            rootCause = rootCause.getCause();
        }
        return rootCause.getMessage() == null
                ? rootCause.getClass().getSimpleName()
                : rootCause.getMessage();
    }
}

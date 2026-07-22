package kz.ask.search.ai_driven.search_query_enrichment.infrastructure.scheduler;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import kz.ask.search.ai_driven.search_query_enrichment.domain.SearchAiEnrichmentClaimService;
import kz.ask.search.ai_driven.search_query_enrichment.domain.SearchAiEnrichmentService;
import kz.ask.search.ai_driven.search_query_enrichment.domain.dto.SearchAiEnrichmentItem;
import kz.ask.search.ai_driven.search_query_enrichment.infrastructure.client.DeepSeekAttributeExtractor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "ask.search.ai-enrichment.enabled", havingValue = "true")
public class SearchAiEnrichmentScheduler {

    private final SearchAiEnrichmentClaimService claimService;
    private final SearchAiEnrichmentService enrichmentService;
    private final DeepSeekAttributeExtractor extractor;

    @Value("${ask.search.ai-enrichment.batch-size:20}")
    private Integer batchSize;

    private final String workerId = UUID.randomUUID().toString();

    @Scheduled(fixedDelayString = "${ask.search.ai-enrichment.interval:PT30S}")
    public void enrich() {
        if (!extractor.isAvailable()) {
            return;
        }
        List<SearchAiEnrichmentItem> items = claimService.claim(batchSize, workerId);
        if (items.isEmpty()) {
            return;
        }
        try {
            Map<UUID, DeepSeekAttributeExtractor.ExtractionResult> results = extractor.extractBatch(
                            items.stream().map(this::toExtractionItem).toList()).stream()
                    .collect(Collectors.toMap(
                            DeepSeekAttributeExtractor.ExtractionResult::getId,
                            Function.identity()));
            items.forEach(item -> enrichmentService.complete(item, results.get(item.getDocumentId())));
        } catch (RuntimeException failure) {
            log.warn("AI search enrichment batch failed: {}", failure.getMessage());
            items.forEach(item -> claimService.retry(item, failure));
        }
    }

    private DeepSeekAttributeExtractor.ExtractionItem toExtractionItem(SearchAiEnrichmentItem item) {
        return DeepSeekAttributeExtractor.ExtractionItem.builder()
                .id(item.getDocumentId())
                .title(item.getTitle())
                .description(item.getDescription())
                .categoryLabel(item.getCategoryLabel())
                .aliases(item.getAliases())
                .build();
    }
}

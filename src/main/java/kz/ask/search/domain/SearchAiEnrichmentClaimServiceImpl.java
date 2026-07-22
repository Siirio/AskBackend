package kz.ask.search.domain;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import kz.ask.search.domain.dto.SearchAiEnrichmentItem;
import kz.ask.search.domain.entity.SearchDocument;
import kz.ask.search.domain.enums.SearchAggregateType;
import kz.ask.search.domain.enums.SearchDocumentType;
import kz.ask.search.infrastructure.repository.SearchDocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SearchAiEnrichmentClaimServiceImpl implements SearchAiEnrichmentClaimService {

    private static final Integer ERROR_LIMIT = 2000;

    private final SearchDocumentRepository repository;

    @Value("${ask.search.ai-enrichment.processing-timeout:PT5M}")
    private Duration processingTimeout;

    @Value("${ask.search.ai-enrichment.retry-base-delay:PT1M}")
    private Duration retryBaseDelay;

    @Value("${ask.search.ai-enrichment.retry-max-delay:PT6H}")
    private Duration retryMaxDelay;

    @Value("${ask.search.ai-enrichment.max-attempts:5}")
    private Integer maxAttempts;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public List<SearchAiEnrichmentItem> claim(Integer batchSize, String workerId) {
        Instant now = Instant.now();
        List<SearchDocument> documents = repository.lockAiEnrichmentBatch(
                now, now.minus(processingTimeout), batchSize);
        documents.forEach(document -> {
            document.setAiEnrichmentStartedAt(now);
            document.setAiEnrichmentWorkerId(workerId);
            document.setAiEnrichmentAttemptCount(document.getAiEnrichmentAttemptCount() + 1);
            document.setAiEnrichmentError(null);
        });
        return documents.stream().map(this::toItem).toList();
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void retry(SearchAiEnrichmentItem item, RuntimeException failure) {
        Instant now = Instant.now();
        String error = truncate(failure.getMessage());
        if (item.getAttemptCount() >= maxAttempts) {
            repository.markAiEnrichmentDead(item.getDocumentId(), item.getWorkerId(), error, now);
            return;
        }
        long multiplier = 1L << Math.min(item.getAttemptCount() - 1, 20);
        Duration delay = retryBaseDelay.multipliedBy(multiplier);
        if (delay.compareTo(retryMaxDelay) > 0) {
            delay = retryMaxDelay;
        }
        repository.markAiEnrichmentRetry(
                item.getDocumentId(), item.getWorkerId(), now.plus(delay),
                error, now);
    }

    private SearchAiEnrichmentItem toItem(SearchDocument document) {
        return SearchAiEnrichmentItem.builder()
                .documentId(document.getId())
                .aggregateType(document.getDocumentType() == SearchDocumentType.PRODUCT
                        ? SearchAggregateType.PRODUCT_OFFER
                        : SearchAggregateType.SERVICE_BRANCH_OFFER)
                .aggregateId(document.getAggregateId())
                .updatedAtMillis(document.getUpdatedAt().toEpochMilli())
                .title(document.getTitle())
                .description(document.getSummary())
                .categoryLabel(document.getCategoryLabel())
                .aliases(document.getAliases())
                .attemptCount(document.getAiEnrichmentAttemptCount())
                .workerId(document.getAiEnrichmentWorkerId())
                .build();
    }

    private String truncate(String value) {
        if (value == null || value.length() <= ERROR_LIMIT) {
            return value;
        }
        return value.substring(0, ERROR_LIMIT);
    }
}

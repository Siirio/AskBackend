package kz.ask.search.basic.domain;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import kz.ask.search.basic.domain.dto.DeadEventRepairBatch;
import kz.ask.search.basic.domain.dto.SearchReconciliationBatch;
import kz.ask.search.basic.domain.entity.SearchDocument;
import kz.ask.search.basic.domain.entity.SearchOutboxEvent;
import kz.ask.search.basic.domain.enums.SearchAggregateType;
import kz.ask.search.basic.domain.enums.SearchDocumentType;
import kz.ask.search.basic.domain.enums.SearchEventType;
import kz.ask.search.basic.domain.enums.SearchOutboxStatus;
import kz.ask.search.basic.infrastructure.repository.SearchDocumentRepository;
import kz.ask.search.basic.infrastructure.repository.SearchOutboxEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SearchReconciliationServiceImpl implements SearchReconciliationService {

    private static final int DEFAULT_BATCH_SIZE = 50;

    private final SearchDocumentRepository searchDocumentRepository;
    private final SearchOutboxEventRepository outboxEventRepository;
    private final SearchOutboxService outboxService;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public SearchReconciliationBatch reconcileProducts(UUID cursor, Integer batchSize, Boolean repair) {
        return reconcileType(SearchDocumentType.ITEM, SearchAggregateType.PRODUCT_OFFER, repair);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public SearchReconciliationBatch reconcileServices(UUID cursor, Integer batchSize, Boolean repair) {
        return reconcileType(SearchDocumentType.SERVICE, SearchAggregateType.SERVICE_BRANCH_OFFER, repair);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public DeadEventRepairBatch repairDeadEvents(Integer batchSize, Boolean repair) {
        int safeSize = Math.min(Math.max(batchSize == null ? DEFAULT_BATCH_SIZE : batchSize, 1), 200);
        List<SearchOutboxEvent> deadEvents = outboxEventRepository
                .findByStatus(SearchOutboxStatus.DEAD.name(), safeSize);
        int requeued = 0;
        int completed = 0;
        int skipped = 0;
        Instant now = Instant.now();

        for (SearchOutboxEvent event : deadEvents) {
            if (event.getEventType() == SearchEventType.UPSERT) {
                SearchDocument doc = searchDocumentRepository
                        .findProjectionByAggregate(toDocumentType(event.getAggregateType()), event.getAggregateId())
                        .orElse(null);
                if (doc != null) {
                    if (Boolean.TRUE.equals(repair)) {
                        outboxService.republish(event.getAggregateType(), event.getAggregateId(),
                                SearchEventType.UPSERT, doc.getProjectionVersion());
                    }
                    requeued++;
                } else {
                    skipped++;
                }
            } else if (event.getEventType() == SearchEventType.DELETE) {
                SearchDocument doc = searchDocumentRepository
                        .findProjectionByAggregate(toDocumentType(event.getAggregateType()), event.getAggregateId())
                        .orElse(null);
                if (doc == null) {
                    if (Boolean.TRUE.equals(repair)) {
                        outboxEventRepository.markDeadCompleted(event.getId(), now,
                                "Aggregate deleted — SearchDocument no longer exists");
                    }
                    completed++;
                } else {
                    if (Boolean.TRUE.equals(repair)) {
                        outboxService.republish(event.getAggregateType(), event.getAggregateId(),
                                SearchEventType.DELETE, Instant.now().toEpochMilli());
                    }
                    requeued++;
                }
            } else {
                skipped++;
            }
        }

        return DeadEventRepairBatch.builder()
                .scanned(deadEvents.size())
                .requeued(requeued)
                .completed(completed)
                .skipped(skipped)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Long activeProjectionCount() {
        return searchDocumentRepository.count();
    }

    private SearchReconciliationBatch reconcileType(SearchDocumentType type, SearchAggregateType aggregateType,
                                                     Boolean repair) {
        List<SearchDocument> documents = searchDocumentRepository.findByDocumentTypeAndAggregateIdIn(type, List.of());
        int mismatches = 0;
        int repairs = 0;
        for (SearchDocument document : documents) {
            if (document.getIndexedAt() != null) {
                continue;
            }
            mismatches++;
            if (Boolean.TRUE.equals(repair)) {
                outboxService.republish(
                        aggregateType,
                        document.getAggregateId(),
                        SearchEventType.UPSERT,
                        document.getProjectionVersion());
                repairs++;
            }
        }
        return SearchReconciliationBatch.builder()
                .nextCursor(null)
                .hasMore(false)
                .scanned(documents.size())
                .mismatches(mismatches)
                .repairsQueued(repairs)
                .build();
    }

    private SearchDocumentType toDocumentType(SearchAggregateType aggregateType) {
        return switch (aggregateType) {
            case PRODUCT_OFFER -> SearchDocumentType.ITEM;
            case SERVICE_BRANCH_OFFER -> SearchDocumentType.SERVICE;
        };
    }
}

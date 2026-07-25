package kz.ask.search.basic.domain;

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
import org.springframework.data.domain.PageRequest;
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
        return reconcileType(SearchDocumentType.ITEM, SearchAggregateType.ITEM, cursor, batchSize, repair);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public SearchReconciliationBatch reconcileServices(UUID cursor, Integer batchSize, Boolean repair) {
        return reconcileType(SearchDocumentType.SERVICE, SearchAggregateType.SERVICE, cursor, batchSize, repair);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public DeadEventRepairBatch repairDeadEvents(Integer batchSize, Boolean repair) {
        int safeSize = Math.min(Math.max(batchSize == null ? DEFAULT_BATCH_SIZE : batchSize, 1), 200);
        List<SearchOutboxEvent> deadEvents = outboxEventRepository
                .findByStatus(SearchOutboxStatus.DEAD.name(), safeSize);
        int wouldRebuild = 0;
        int wouldDelete = 0;
        int wouldSupersede = 0;
        int skipped = 0;
        int requeued = 0;
        int completed = 0;

        for (SearchOutboxEvent event : deadEvents) {
            SearchDocument doc = searchDocumentRepository
                    .findProjectionByAggregate(toDocumentType(event.getAggregateType()), event.getAggregateId())
                    .orElse(null);

            if (event.getEventType() == SearchEventType.UPSERT) {
                if (doc != null) {
                    if (doc.getProjectionVersion() > event.getAggregateVersion()) {
                        wouldSupersede++;
                        skipped++;
                        continue;
                    }
                    wouldRebuild++;
                    if (Boolean.TRUE.equals(repair)) {
                        outboxService.republish(event.getAggregateType(), event.getAggregateId(),
                                SearchEventType.UPSERT, doc.getProjectionVersion());
                        requeued++;
                    }
                } else {
                    wouldDelete++;
                    skipped++;
                }
            } else if (event.getEventType() == SearchEventType.DELETE) {
                if (doc == null) {
                    wouldDelete++;
                    if (Boolean.TRUE.equals(repair)) {
                        outboxEventRepository.markDeadCompleted(event.getId(), java.time.Instant.now(),
                                "Aggregate deleted — SearchDocument no longer exists");
                        completed++;
                    }
                } else {
                    wouldRebuild++;
                    if (Boolean.TRUE.equals(repair)) {
                        outboxService.republish(event.getAggregateType(), event.getAggregateId(),
                                SearchEventType.DELETE, doc.getProjectionVersion());
                        requeued++;
                    }
                }
            } else {
                skipped++;
            }
        }

        return DeadEventRepairBatch.builder()
                .scanned(deadEvents.size())
                .wouldRebuild(wouldRebuild)
                .wouldDelete(wouldDelete)
                .wouldSupersede(wouldSupersede)
                .skipped(skipped)
                .requeued(requeued)
                .completed(completed)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Long activeProjectionCount() {
        return searchDocumentRepository.count();
    }

    private SearchReconciliationBatch reconcileType(SearchDocumentType type, SearchAggregateType aggregateType,
                                                     UUID cursor, Integer batchSize, Boolean repair) {
        int safeSize = Math.min(Math.max(batchSize == null ? DEFAULT_BATCH_SIZE : batchSize, 1), 200);
        List<SearchDocument> batch = searchDocumentRepository.findReconciliationBatch(type, cursor,
                PageRequest.of(0, safeSize));
        int mismatches = 0;
        int repairs = 0;
        UUID lastAggregateId = null;

        for (SearchDocument doc : batch) {
            lastAggregateId = doc.getAggregateId();
            long indexedVersion = doc.getIndexedVersion() == null ? 0L : doc.getIndexedVersion();
            if (doc.getProjectionVersion() <= indexedVersion) {
                continue;
            }
            mismatches++;
            if (Boolean.TRUE.equals(repair)) {
                outboxService.republish(aggregateType, doc.getAggregateId(),
                        SearchEventType.UPSERT, doc.getProjectionVersion());
                repairs++;
            }
        }

        boolean hasMore = batch.size() >= safeSize;
        return SearchReconciliationBatch.builder()
                .nextCursor(hasMore ? lastAggregateId : null)
                .hasMore(hasMore)
                .scanned(batch.size())
                .mismatches(mismatches)
                .repairsQueued(repairs)
                .build();
    }

    private SearchDocumentType toDocumentType(SearchAggregateType aggregateType) {
        return switch (aggregateType) {
            case ITEM -> SearchDocumentType.ITEM;
            case SERVICE -> SearchDocumentType.SERVICE;
            case BUSINESS -> SearchDocumentType.BUSINESS;
            case UNIQUE_OFFER -> SearchDocumentType.UNIQUE_OFFER;
        };
    }
}

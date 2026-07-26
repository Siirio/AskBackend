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
import kz.ask.search.basic.domain.enums.SearchProjectionAction;
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
    public SearchReconciliationBatch reconcileItems(UUID cursor, Integer batchSize, Boolean repair) {
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

        for (SearchOutboxEvent event : deadEvents) {
            SearchDocument document = searchDocumentRepository
                    .findProjectionByAggregate(toDocumentType(event.getAggregateType()), event.getAggregateId())
                    .orElse(null);
            if (document == null) {
                skipped++;
                continue;
            }
            if (document.getProjectionVersion() > event.getAggregateVersion()) {
                wouldSupersede++;
            }
            SearchEventType desiredEvent = document.getProjectionAction() == SearchProjectionAction.INDEX
                    ? SearchEventType.UPSERT : SearchEventType.DELETE;
            if (desiredEvent == SearchEventType.UPSERT) {
                wouldRebuild++;
            } else {
                wouldDelete++;
            }
            if (Boolean.TRUE.equals(repair)) {
                outboxService.republish(event.getAggregateType(), event.getAggregateId(),
                        desiredEvent, document.getProjectionVersion());
                requeued++;
            }
        }

        return DeadEventRepairBatch.builder()
                .scanned(deadEvents.size())
                .wouldRebuild(wouldRebuild)
                .wouldDelete(wouldDelete)
                .wouldSupersede(wouldSupersede)
                .skipped(skipped)
                .requeued(requeued)
                .completed(0)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Long activeProjectionCount() {
        return searchDocumentRepository.countByProjectionAction(SearchProjectionAction.INDEX);
    }

    private SearchReconciliationBatch reconcileType(SearchDocumentType type, SearchAggregateType aggregateType,
                                                     UUID cursor, Integer batchSize, Boolean repair) {
        int safeSize = Math.min(Math.max(batchSize == null ? DEFAULT_BATCH_SIZE : batchSize, 1), 200);
        List<SearchDocument> batch = searchDocumentRepository.findReconciliationBatch(
                type, cursor, PageRequest.of(0, safeSize));
        int mismatches = 0;
        int repairs = 0;
        UUID lastAggregateId = null;

        for (SearchDocument document : batch) {
            lastAggregateId = document.getAggregateId();
            long indexedVersion = document.getIndexedVersion() == null ? 0L : document.getIndexedVersion();
            if (document.getProjectionVersion() <= indexedVersion) {
                continue;
            }
            mismatches++;
            if (Boolean.TRUE.equals(repair)) {
                SearchEventType eventType = document.getProjectionAction() == SearchProjectionAction.INDEX
                        ? SearchEventType.UPSERT : SearchEventType.DELETE;
                outboxService.republish(aggregateType, document.getAggregateId(),
                        eventType, document.getProjectionVersion());
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
        return aggregateType == SearchAggregateType.ITEM
                ? SearchDocumentType.ITEM : SearchDocumentType.SERVICE;
    }
}

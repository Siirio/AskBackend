package kz.ask.search.basic.domain;

import java.util.List;
import java.util.UUID;
import kz.ask.search.basic.domain.dto.SearchReconciliationBatch;
import kz.ask.search.basic.domain.entity.SearchDocument;
import kz.ask.search.basic.domain.enums.SearchAggregateType;
import kz.ask.search.basic.domain.enums.SearchDocumentType;
import kz.ask.search.basic.domain.enums.SearchEventType;
import kz.ask.search.basic.infrastructure.repository.SearchDocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SearchReconciliationServiceImpl implements SearchReconciliationService {

    private final SearchDocumentRepository searchDocumentRepository;
    private final SearchOutboxService outboxService;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public SearchReconciliationBatch reconcileProducts(UUID cursor, Integer batchSize, Boolean repair) {
        return reconcileType(SearchDocumentType.PRODUCT, SearchAggregateType.PRODUCT_OFFER, repair);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public SearchReconciliationBatch reconcileServices(UUID cursor, Integer batchSize, Boolean repair) {
        return reconcileType(SearchDocumentType.SERVICE, SearchAggregateType.SERVICE_BRANCH_OFFER, repair);
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
                        document.getUpdatedAt().toEpochMilli());
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
}

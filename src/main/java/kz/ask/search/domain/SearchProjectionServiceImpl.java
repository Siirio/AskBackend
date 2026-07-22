package kz.ask.search.domain;

import java.time.Instant;
import java.util.UUID;
import kz.ask.search.domain.dto.SearchOutboxEventDto;
import kz.ask.search.domain.dto.SearchProjectionResult;
import kz.ask.search.domain.entity.SearchDocument;
import kz.ask.search.domain.enums.SearchAvailabilitySource;
import kz.ask.search.domain.enums.SearchAvailabilityStatus;
import kz.ask.search.domain.enums.SearchDocumentType;
import kz.ask.search.domain.enums.SearchEventType;
import kz.ask.search.domain.enums.SearchProjectionAction;
import kz.ask.search.infrastructure.repository.SearchDocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SearchProjectionServiceImpl implements SearchProjectionService {

    private static final String PROJECTION_SOURCE = "CATALOG";

    private final SearchDocumentRepository searchDocumentRepository;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public SearchProjectionResult apply(SearchOutboxEventDto event) {
        SearchDocumentType type = toDocumentType(event.getAggregateType());
        if (type == null) {
            return SearchProjectionResult.builder()
                    .action(SearchProjectionAction.STALE)
                    .aggregateId(event.getAggregateId())
                    .build();
        }

        if (event.getEventType() == SearchEventType.DELETE) {
            return archiveMissing(type, event);
        }

        SearchDocument document = findOrCreate(type, event.getAggregateId());
        if (isStale(document, event.getAggregateVersion())) {
            return SearchProjectionResult.builder()
                    .action(SearchProjectionAction.STALE)
                    .documentType(type)
                    .aggregateId(event.getAggregateId())
                    .build();
        }

        prepareForVersion(document, event.getAggregateVersion());
        document.setSource(PROJECTION_SOURCE);
        document.setAvailabilityStatus(SearchAvailabilityStatus.UNKNOWN);
        document.setAvailabilitySource(SearchAvailabilitySource.UNKNOWN);
        SearchDocument saved = searchDocumentRepository.save(document);
        return SearchProjectionResult.builder()
                .action(SearchProjectionAction.INDEX)
                .documentType(saved.getDocumentType())
                .aggregateId(saved.getAggregateId())
                .documentId(saved.getId())
                .build();
    }

    private SearchProjectionResult archiveMissing(SearchDocumentType type, SearchOutboxEventDto event) {
        SearchDocument document = searchDocumentRepository.findProjectionByAggregate(type, event.getAggregateId()).orElse(null);
        if (document == null) {
            return SearchProjectionResult.builder()
                    .action(SearchProjectionAction.STALE)
                    .documentType(type)
                    .aggregateId(event.getAggregateId())
                    .build();
        }
        if (isStale(document, event.getAggregateVersion())) {
            return SearchProjectionResult.builder()
                    .action(SearchProjectionAction.STALE)
                    .documentType(type)
                    .aggregateId(event.getAggregateId())
                    .build();
        }
        SearchDocument saved = searchDocumentRepository.save(document);
        return SearchProjectionResult.builder()
                .action(SearchProjectionAction.DELETE)
                .documentType(saved.getDocumentType())
                .aggregateId(saved.getAggregateId())
                .documentId(saved.getId())
                .build();
    }

    private SearchDocument findOrCreate(SearchDocumentType type, UUID aggregateId) {
        return searchDocumentRepository.findProjectionByAggregate(type, aggregateId)
                .orElseGet(() -> {
                    SearchDocument document = new SearchDocument();
                    document.setDocumentType(type);
                    document.setAggregateId(aggregateId);
                    return document;
                });
    }

    private boolean isStale(SearchDocument document, Long aggregateVersion) {
        return document.getId() != null
                && document.getUpdatedAt() != null
                && document.getUpdatedAt().toEpochMilli() > aggregateVersion;
    }

    private void prepareForVersion(SearchDocument document, Long version) {
        boolean isNewVersion = document.getId() == null
                || document.getUpdatedAt() == null
                || version > document.getUpdatedAt().toEpochMilli();
        if (isNewVersion) {
            document.setAiEnrichmentAvailableAt(Instant.now());
            document.setAiEnrichmentStartedAt(null);
            document.setAiEnrichmentWorkerId(null);
            document.setAiEnrichmentAttemptCount(0);
            document.setAiEnrichmentError(null);
            document.setAiEnrichmentDead(Boolean.FALSE);
            document.setAiEnrichmentRequested(Boolean.FALSE);
        }
    }

    private SearchDocumentType toDocumentType(kz.ask.search.domain.enums.SearchAggregateType aggregateType) {
        return switch (aggregateType) {
            case PRODUCT_OFFER -> SearchDocumentType.PRODUCT;
            case SERVICE_BRANCH_OFFER -> SearchDocumentType.SERVICE;
        };
    }
}

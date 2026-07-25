package kz.ask.search.basic.domain;

import java.time.Instant;
import kz.ask.search.basic.domain.dto.SearchOutboxEventDto;
import kz.ask.search.basic.domain.dto.SearchProjectionResult;
import kz.ask.search.basic.domain.entity.SearchDocument;
import kz.ask.search.basic.domain.enums.SearchDocumentType;
import kz.ask.search.basic.domain.enums.SearchProjectionAction;
import kz.ask.search.basic.infrastructure.mapper.MeilisearchDocumentMapper;
import kz.ask.search.basic.infrastructure.repository.SearchDocumentRepository;
import kz.ask.shared.error.ExternalServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SearchIndexDeliveryServiceImpl implements SearchIndexDeliveryService {

    private final SearchDocumentRepository searchDocumentRepository;
    private final MeilisearchService meilisearchService;
    private final MeilisearchDocumentMapper documentMapper;
    private final SearchIndexDeliveryServiceImpl self;

    @Override
    public String deliver(SearchOutboxEventDto event, SearchProjectionResult projection) {
        if (projection.getAction() == SearchProjectionAction.DELETE) {
            return deliverDelete(event, projection);
        }
        return deliverIndex(event, projection);
    }

    private String deliverDelete(SearchOutboxEventDto event, SearchProjectionResult projection) {
        boolean shouldDelete = self.verifyDeleteNotStale(projection.getDocumentType(),
                event.getAggregateId(), event.getAggregateVersion());
        if (!shouldDelete) {
            return "DELETE is stale — newer live projection exists";
        }
        callMeilisearchDelete(event.getAggregateId());
        self.confirmDelete(event);
        return "DELETE";
    }

    private String deliverIndex(SearchOutboxEventDto event, SearchProjectionResult projection) {
        var payload = self.buildIndexPayload(projection.getDocumentType(),
                event.getAggregateId(), event.getAggregateVersion());
        if (payload == null) {
            return "Projection no longer exists or version is stale";
        }
        callMeilisearchIndex(payload);
        self.confirmIndexedVersion(projection.getDocumentType(),
                event.getAggregateId(), event.getAggregateVersion());
        return "INDEX";
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean verifyDeleteNotStale(SearchDocumentType documentType, java.util.UUID aggregateId,
                                         Long eventVersion) {
        SearchDocument doc = searchDocumentRepository
                .findProjectionByAggregate(documentType, aggregateId).orElse(null);
        if (doc != null && doc.getProjectionVersion() > eventVersion) {
            return false;
        }
        return true;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public MeilisearchIndexDocument buildIndexPayload(SearchDocumentType documentType,
                                                       java.util.UUID aggregateId,
                                                       Long eventVersion) {
        SearchDocument doc = searchDocumentRepository
                .findProjectionByAggregate(documentType, aggregateId).orElse(null);
        if (doc == null) {
            return null;
        }
        if (doc.getProjectionVersion() > eventVersion) {
            return null;
        }
        return documentMapper.toIndexDocument(doc);
    }

    private void callMeilisearchIndex(MeilisearchIndexDocument indexDoc) {
        try {
            meilisearchService.index(indexDoc);
        } catch (RuntimeException e) {
            throw new ExternalServiceException("Meilisearch index failed", e);
        }
    }

    private void callMeilisearchDelete(java.util.UUID aggregateId) {
        try {
            meilisearchService.delete(aggregateId);
        } catch (RuntimeException e) {
            throw new ExternalServiceException("Meilisearch delete failed", e);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void confirmIndexedVersion(SearchDocumentType documentType,
                                       java.util.UUID aggregateId,
                                       Long deliveredVersion) {
        searchDocumentRepository.findProjectionByAggregate(documentType, aggregateId)
                .ifPresent(doc -> {
                    if (doc.getProjectionVersion().equals(deliveredVersion)) {
                        doc.setIndexedVersion(deliveredVersion);
                        doc.setIndexedAt(Instant.now());
                    }
                });
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void confirmDelete(SearchOutboxEventDto event) {
        searchDocumentRepository.findProjectionByAggregate(
                SearchDocumentType.ITEM, event.getAggregateId())
                .ifPresent(doc -> {
                    if (doc.getProjectionVersion() <= event.getAggregateVersion()) {
                        searchDocumentRepository.delete(doc);
                    }
                });
    }
}

package kz.ask.search.domain;

import java.time.Instant;
import kz.ask.search.domain.dto.SearchOutboxEventDto;
import kz.ask.search.domain.dto.SearchProjectionResult;
import kz.ask.search.domain.entity.SearchDocument;
import kz.ask.search.domain.enums.SearchProjectionAction;
import kz.ask.search.infrastructure.repository.SearchAggregateLockRepository;
import kz.ask.search.infrastructure.repository.SearchDocumentRepository;
import kz.ask.search.infrastructure.mapper.MeilisearchDocumentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SearchIndexDeliveryServiceImpl implements SearchIndexDeliveryService {

    private final SearchAggregateLockRepository lockRepository;
    private final SearchDocumentRepository searchDocumentRepository;
    private final MeilisearchService meilisearchService;
    private final MeilisearchDocumentMapper documentMapper;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public String deliver(SearchOutboxEventDto event, SearchProjectionResult projection) {
        lockRepository.lock(event.getAggregateId());
        SearchDocument document = searchDocumentRepository
                .findProjectionByAggregate(projection.getDocumentType(), event.getAggregateId())
                .orElse(null);
        if (document == null) {
            return "Projection no longer exists";
        }
        if (document.getUpdatedAt().toEpochMilli() > event.getAggregateVersion()) {
            return "Projection advanced before index delivery";
        }
        if (projection.getAction() == SearchProjectionAction.DELETE) {
            meilisearchService.delete(document.getId());
        } else {
            meilisearchService.index(documentMapper.toIndexDocument(document));
        }
        document.setIndexedAt(Instant.now());
        return projection.getAction().name();
    }

}

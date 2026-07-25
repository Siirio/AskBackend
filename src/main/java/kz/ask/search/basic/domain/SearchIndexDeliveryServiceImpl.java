package kz.ask.search.basic.domain;

import java.time.Instant;
import kz.ask.search.basic.domain.dto.SearchOutboxEventDto;
import kz.ask.search.basic.domain.dto.SearchProjectionResult;
import kz.ask.search.basic.domain.entity.SearchDocument;
import kz.ask.search.basic.domain.enums.SearchProjectionAction;
import kz.ask.search.basic.infrastructure.mapper.MeilisearchDocumentMapper;
import kz.ask.search.basic.infrastructure.repository.SearchAggregateLockRepository;
import kz.ask.search.basic.infrastructure.repository.SearchDocumentRepository;
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
        if (projection.getAction() == SearchProjectionAction.DELETE) {
            meilisearchService.delete(event.getAggregateId());
            return projection.getAction().name();
        }
        lockRepository.lock(event.getAggregateId());
        SearchDocument document = searchDocumentRepository
                .findProjectionByAggregate(projection.getDocumentType(), event.getAggregateId())
                .orElse(null);
        if (document == null) {
            return "Projection no longer exists";
        }
        Long projectionVersion = document.getProjectionVersion();
        if (projectionVersion != null && projectionVersion > event.getAggregateVersion()) {
            return "Projection advanced before index delivery";
        }
        meilisearchService.index(documentMapper.toIndexDocument(document));
        document.setIndexedAt(Instant.now());
        return projection.getAction().name();
    }

}

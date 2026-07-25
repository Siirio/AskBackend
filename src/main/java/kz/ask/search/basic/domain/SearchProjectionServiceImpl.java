package kz.ask.search.basic.domain;

import java.util.UUID;
import kz.ask.search.basic.domain.dto.SearchOutboxEventDto;
import kz.ask.search.basic.domain.dto.SearchProjectionResult;
import kz.ask.search.basic.domain.entity.SearchDocument;
import kz.ask.search.basic.domain.enums.SearchDocumentType;
import kz.ask.search.basic.domain.enums.SearchEventType;
import kz.ask.search.basic.domain.enums.SearchProjectionAction;
import kz.ask.search.basic.infrastructure.repository.SearchDocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SearchProjectionServiceImpl implements SearchProjectionService {

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
            return SearchProjectionResult.builder()
                    .action(SearchProjectionAction.DELETE)
                    .documentType(type)
                    .aggregateId(event.getAggregateId())
                    .build();
        }

        SearchDocument document = searchDocumentRepository
                .findProjectionByAggregate(type, event.getAggregateId())
                .orElse(null);
        if (document == null) {
            return SearchProjectionResult.builder()
                    .action(SearchProjectionAction.STALE)
                    .documentType(type)
                    .aggregateId(event.getAggregateId())
                    .build();
        }

        Long projectionVersion = document.getProjectionVersion();
        if (projectionVersion != null && projectionVersion > event.getAggregateVersion()) {
            return SearchProjectionResult.builder()
                    .action(SearchProjectionAction.STALE)
                    .documentType(type)
                    .aggregateId(event.getAggregateId())
                    .documentId(document.getId())
                    .build();
        }

        return SearchProjectionResult.builder()
                .action(SearchProjectionAction.INDEX)
                .documentType(document.getDocumentType())
                .aggregateId(document.getAggregateId())
                .documentId(document.getId())
                .build();
    }

    private SearchDocumentType toDocumentType(kz.ask.search.basic.domain.enums.SearchAggregateType aggregateType) {
        return switch (aggregateType) {
            case ITEM -> SearchDocumentType.ITEM;
            case SERVICE -> SearchDocumentType.SERVICE;
            case BUSINESS -> SearchDocumentType.BUSINESS;
            case UNIQUE_OFFER -> SearchDocumentType.UNIQUE_OFFER;
        };
    }
}

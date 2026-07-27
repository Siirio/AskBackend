package kz.ask.search.basic.domain;

import java.util.Optional;
import kz.ask.search.basic.application.processor.SearchDeliveryConfirmationProcessor;
import kz.ask.search.basic.application.processor.SearchSemanticEnrichmentProcessor;
import kz.ask.search.basic.domain.dto.SearchDocumentDto;
import kz.ask.search.basic.domain.dto.SearchOutboxEventDto;
import kz.ask.search.basic.domain.enums.SearchAggregateType;
import kz.ask.search.basic.domain.enums.SearchDocumentType;
import kz.ask.search.basic.domain.enums.SearchEventType;
import kz.ask.search.basic.domain.enums.SearchProjectionAction;
import kz.ask.search.basic.infrastructure.mapper.MeilisearchDocumentMapper;
import kz.ask.search.basic.infrastructure.meilisearch.MeilisearchIndexGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SearchIndexDeliveryServiceImpl implements SearchIndexDeliveryService {

    private final SearchDocumentService searchDocumentService;
    private final MeilisearchIndexGateway meilisearchIndexGateway;
    private final MeilisearchDocumentMapper documentMapper;
    private final SearchDeliveryConfirmationProcessor confirmationProcessor;
    private final SearchSemanticEnrichmentProcessor semanticEnrichmentProcessor;

    @Override
    public String deliver(SearchOutboxEventDto event) {
        SearchDocumentType documentType = event.getAggregateType() == SearchAggregateType.ITEM
                ? SearchDocumentType.ITEM : SearchDocumentType.SERVICE;
        SearchProjectionAction action = event.getEventType() == SearchEventType.UPSERT
                ? SearchProjectionAction.INDEX : SearchProjectionAction.DELETE;
        Optional<SearchDocumentDto> prepared = searchDocumentService.prepareDelivery(
                documentType, event.getAggregateId(), event.getAggregateVersion(), action);
        if (prepared.isEmpty()) {
            return SearchProjectionAction.STALE.name();
        }
        if (action == SearchProjectionAction.INDEX) {
            if (semanticEnrichmentProcessor.enrichIfRequired(prepared.get())) {
                return "SEMANTIC_ENRICHMENT";
            }
            meilisearchIndexGateway.index(documentMapper.toIndexDocument(prepared.get()));
        } else {
            meilisearchIndexGateway.delete(event.getAggregateId());
        }
        confirmationProcessor.confirm(event, action);
        return action.name();
    }
}

package kz.ask.search.basic.application.processor;

import java.util.Optional;
import kz.ask.search.basic.domain.SearchDocumentService;
import kz.ask.search.basic.domain.SearchOutboxService;
import kz.ask.search.basic.domain.dto.SearchDocumentDto;
import kz.ask.search.basic.domain.dto.SearchOutboxEventDto;
import kz.ask.search.basic.domain.enums.SearchEventType;
import kz.ask.search.basic.domain.enums.SearchAggregateType;
import kz.ask.search.basic.domain.enums.SearchDocumentType;
import kz.ask.search.basic.domain.enums.SearchProjectionAction;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class SearchDeliveryConfirmationProcessor {

    private final SearchDocumentService searchDocumentService;
    private final SearchOutboxService searchOutboxService;

    @Transactional
    public void confirm(SearchOutboxEventDto event, SearchProjectionAction deliveredAction) {
        Optional<SearchDocumentDto> correction = searchDocumentService.confirmDelivery(
                event.getAggregateType() == SearchAggregateType.ITEM
                        ? SearchDocumentType.ITEM : SearchDocumentType.SERVICE,
                event.getAggregateId(),
                event.getAggregateVersion(),
                deliveredAction);
        correction.ifPresent(current -> searchOutboxService.republish(
                event.getAggregateType(),
                event.getAggregateId(),
                current.getProjectionAction() == SearchProjectionAction.INDEX
                        ? SearchEventType.UPSERT : SearchEventType.DELETE,
                current.getProjectionVersion()));
    }
}

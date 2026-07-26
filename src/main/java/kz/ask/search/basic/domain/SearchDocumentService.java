package kz.ask.search.basic.domain;

import java.util.Optional;
import java.util.UUID;
import kz.ask.search.basic.domain.dto.SearchDocumentDto;
import kz.ask.search.basic.domain.enums.SearchDocumentType;
import kz.ask.search.basic.domain.enums.SearchProjectionAction;

public interface SearchDocumentService {

    Long upsert(SearchDocumentDto dto);

    Long delete(SearchDocumentType documentType, UUID aggregateId);

    Optional<SearchDocumentDto> findByAggregate(SearchDocumentType type, UUID aggregateId);

    Optional<SearchDocumentDto> prepareDelivery(SearchDocumentType type, UUID aggregateId,
                                                Long eventVersion, SearchProjectionAction action);

    Optional<SearchDocumentDto> confirmDelivery(SearchDocumentType type, UUID aggregateId,
                                                Long eventVersion, SearchProjectionAction action);
}

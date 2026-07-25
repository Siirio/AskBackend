package kz.ask.search.basic.domain;

import java.util.Optional;
import java.util.UUID;
import kz.ask.search.basic.domain.entity.SearchDocument;
import kz.ask.search.basic.domain.enums.SearchDocumentType;

public interface SearchDocumentService {

    Long upsertItemProjection(SearchableItemSource source);

    void deleteItemProjection(UUID itemId);

    Long upsertServiceProjection(SearchableServiceSource source);

    void deleteServiceProjection(UUID serviceOfferingId);

    Optional<SearchDocument> findByAggregate(SearchDocumentType type, UUID aggregateId);
}

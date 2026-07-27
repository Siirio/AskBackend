package kz.ask.search.basic.infrastructure.meilisearch;

import java.util.List;
import java.util.UUID;
import kz.ask.search.basic.application.processor.SearchPlan;
import kz.ask.search.basic.domain.dto.MeilisearchIndexDocument;
import kz.ask.search.basic.domain.dto.SearchHitDto;

public interface MeilisearchIndexGateway {

    void index(MeilisearchIndexDocument document);

    void delete(UUID documentId);

    void indexAll(List<MeilisearchIndexDocument> documents);

    String createRebuildIndex();

    void indexAll(String targetIndex, List<MeilisearchIndexDocument> documents);

    void activateRebuildIndex(String rebuildIndex);

    void discardIndex(String targetIndex);

    List<SearchHitDto> search(SearchPlan plan, int limit);
}

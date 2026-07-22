package kz.ask.search.ai_driven.search_query_enrichment.infrastructure.repository;

import java.util.List;
import java.util.UUID;
import kz.ask.search.ai_driven.search_query_enrichment.domain.entity.SearchAiMetadata;
import kz.ask.search.ai_driven.search_query_enrichment.domain.enums.SearchAiMetadataSource;
import kz.ask.search.basic.domain.enums.SearchAggregateType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SearchAiMetadataRepository extends JpaRepository<SearchAiMetadata, UUID> {

    List<SearchAiMetadata> findByAggregateTypeAndAggregateIdAndSource(
            SearchAggregateType aggregateType, UUID aggregateId, SearchAiMetadataSource source);
}

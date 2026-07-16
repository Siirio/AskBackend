package kz.ask.search.infrastructure.repository;

import java.util.List;
import java.util.UUID;
import kz.ask.search.domain.entity.SearchAiMetadata;
import kz.ask.search.domain.enums.SearchAggregateType;
import kz.ask.search.domain.enums.SearchAiMetadataSource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SearchAiMetadataRepository extends JpaRepository<SearchAiMetadata, UUID> {

    List<SearchAiMetadata> findByAggregateTypeAndAggregateIdAndSource(
            SearchAggregateType aggregateType, UUID aggregateId, SearchAiMetadataSource source);
}

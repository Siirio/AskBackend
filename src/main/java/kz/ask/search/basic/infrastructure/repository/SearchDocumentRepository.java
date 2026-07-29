package kz.ask.search.basic.infrastructure.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import kz.ask.search.basic.domain.entity.SearchDocument;
import kz.ask.search.basic.domain.enums.SearchDocumentType;
import kz.ask.search.basic.domain.enums.SearchProjectionAction;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import jakarta.persistence.LockModeType;

@Repository
public interface SearchDocumentRepository extends JpaRepository<SearchDocument, UUID> {

    Long countByProjectionAction(SearchProjectionAction projectionAction);

    @Query("""
        select distinct d from SearchDocument d
        left join fetch d.business
        left join fetch d.branch b
        left join fetch b.city
        left join fetch d.tokens
        where d.documentType = :documentType and d.aggregateId = :aggregateId
        """)
    Optional<SearchDocument> findProjectionByAggregate(@Param("documentType") SearchDocumentType documentType,
                                                        @Param("aggregateId") UUID aggregateId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        select d from SearchDocument d
        where d.documentType = :documentType and d.aggregateId = :aggregateId
        """)
    Optional<SearchDocument> findProjectionByAggregateForUpdate(
            @Param("documentType") SearchDocumentType documentType,
            @Param("aggregateId") UUID aggregateId);

    @Query(value = """
        select nextval('search_projection_version_seq')
        """, nativeQuery = true)
    Long nextVersion();

    @Query("""
        select distinct d from SearchDocument d
        left join fetch d.business
        left join fetch d.branch b
        left join fetch b.city
        left join fetch d.tokens
        where d.documentType in :types and d.aggregateId in :aggregateIds
          and d.projectionAction = kz.ask.search.basic.domain.enums.SearchProjectionAction.INDEX
        """)
    List<SearchDocument> findAllByDocumentTypeAndAggregateIdIn(
            @Param("types") List<SearchDocumentType> types,
            @Param("aggregateIds") Collection<UUID> aggregateIds);

    @Query("""
        select distinct d from SearchDocument d
        left join fetch d.business
        left join fetch d.branch b
        left join fetch b.city
        left join fetch d.tokens
        where d.projectionAction = kz.ask.search.basic.domain.enums.SearchProjectionAction.INDEX
          and (:afterId is null or d.id > :afterId)
        order by d.id
        """)
    List<SearchDocument> findReindexBatch(@Param("afterId") UUID afterId, Pageable pageable);
}

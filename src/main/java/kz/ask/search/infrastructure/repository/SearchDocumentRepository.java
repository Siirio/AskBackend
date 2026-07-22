package kz.ask.search.infrastructure.repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import kz.ask.search.domain.entity.SearchDocument;
import kz.ask.search.domain.enums.SearchDocumentType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SearchDocumentRepository extends JpaRepository<SearchDocument, UUID> {

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

    @Query("""
            SELECT DISTINCT d FROM SearchDocument d
            LEFT JOIN FETCH d.business
            LEFT JOIN FETCH d.branch b
            LEFT JOIN FETCH b.city
            LEFT JOIN FETCH d.tokens
            WHERE d.id IN :ids
            """)
    List<SearchDocument> findAllByIdIn(@Param("ids") Collection<UUID> ids);

    List<SearchDocument> findByDocumentTypeAndAggregateIdIn(SearchDocumentType documentType,
                                                             Collection<UUID> aggregateIds);

    @Query("""
        select distinct d from SearchDocument d
        left join fetch d.business
        left join fetch d.branch b
        left join fetch b.city
        where (:afterId is null or d.id > :afterId)
        order by d.id
        """)
    List<SearchDocument> findReindexBatch(@Param("afterId") UUID afterId, Pageable pageable);

    @Query(value = """
        select * from search_document
        where ai_enrichment_requested = true
          and ai_enrichment_dead = false
          and ai_enrichment_available_at <= :now
          and (ai_enrichment_started_at is null or ai_enrichment_started_at < :staleBefore)
        order by ai_enrichment_available_at, id
        limit :batchSize
        for update skip locked
        """, nativeQuery = true)
    List<SearchDocument> lockAiEnrichmentBatch(@Param("now") Instant now,
                                                @Param("staleBefore") Instant staleBefore,
                                                @Param("batchSize") Integer batchSize);

    @Modifying
    @Query(value = """
        update search_document
        set ai_enrichment_started_at = null, ai_enrichment_worker_id = null,
            ai_enrichment_available_at = :availableAt, ai_enrichment_error = :error,
            updated_at = :now
        where id = :documentId and ai_enrichment_worker_id = :workerId
        """, nativeQuery = true)
    int markAiEnrichmentRetry(@Param("documentId") UUID documentId,
                              @Param("workerId") String workerId,
                              @Param("availableAt") Instant availableAt,
                              @Param("error") String error,
                              @Param("now") Instant now);

    @Modifying
    @Query(value = """
        update search_document
        set ai_enrichment_started_at = null, ai_enrichment_worker_id = null,
            ai_enrichment_dead = true, ai_enrichment_requested = false,
            ai_enrichment_error = :error, updated_at = :now
        where id = :documentId and ai_enrichment_worker_id = :workerId
        """, nativeQuery = true)
    int markAiEnrichmentDead(@Param("documentId") UUID documentId,
                             @Param("workerId") String workerId,
                             @Param("error") String error,
                             @Param("now") Instant now);

    @Query(value = """
        select d.id
        from search_document d
        left join business_branch branch on branch.id = d.branch_id
        left join city on city.id = branch.city_id
        where d.document_type in (:documentTypes)
          and (:query = ''
               or d.search_vector @@ websearch_to_tsquery('simple', :query)
               or d.normalized_title % :query)
          and (:category = '' or lower(coalesce(d.category_path, d.category_label, '')) like concat('%', :category, '%'))
          and (:minPrice is null or d.price >= :minPrice)
          and (:maxPrice is null or d.price <= :maxPrice)
          and (:city = '' or lower(coalesce(city.name, '')) = :city)
        order by
          ts_rank_cd(d.search_vector, websearch_to_tsquery('simple', :query)) desc,
          similarity(d.normalized_title, :query) desc,
          d.id
        limit :candidateLimit
        """, nativeQuery = true)
    List<UUID> findPostgresCandidateIds(@Param("documentTypes") List<String> documentTypes,
                                        @Param("query") String query,
                                        @Param("category") String category,
                                        @Param("minPrice") BigDecimal minPrice,
                                        @Param("maxPrice") BigDecimal maxPrice,
                                        @Param("city") String city,
                                        @Param("candidateLimit") Integer candidateLimit);
}

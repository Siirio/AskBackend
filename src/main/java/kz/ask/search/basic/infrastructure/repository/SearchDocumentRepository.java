package kz.ask.search.basic.infrastructure.repository;

import java.math.BigDecimal;
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
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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

    @Query("""
            SELECT DISTINCT d FROM SearchDocument d
            LEFT JOIN FETCH d.business
            LEFT JOIN FETCH d.branch b
            LEFT JOIN FETCH b.city
            LEFT JOIN FETCH d.tokens
            WHERE d.id IN :ids AND d.projectionAction = kz.ask.search.basic.domain.enums.SearchProjectionAction.INDEX
            """)
    List<SearchDocument> findAllByIdIn(@Param("ids") Collection<UUID> ids);

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

    @Query(value = """
        select d.id
        from search_document d
        left join business_branch branch on branch.id = d.branch_id
        left join city on city.id = branch.city_id
        where d.document_type in (:documentTypes)
          and d.projection_action = 'INDEX'
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

    @Query(value = """
        select d.id
        from search_document d
        left join business_branch branch on branch.id = d.branch_id
        left join city on city.id = branch.city_id
        where d.document_type in (:documentTypes)
          and d.projection_action = 'INDEX'
          and d.projection_version > coalesce(d.indexed_version, 0)
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
    List<UUID> findDirtyCandidateIds(@Param("documentTypes") List<String> documentTypes,
                                     @Param("query") String query,
                                     @Param("category") String category,
                                     @Param("minPrice") BigDecimal minPrice,
                                     @Param("maxPrice") BigDecimal maxPrice,
                                     @Param("city") String city,
                                     @Param("candidateLimit") Integer candidateLimit);

    @Query("""
        select distinct d from SearchDocument d
        left join fetch d.business
        left join fetch d.branch b
        left join fetch b.city
        left join fetch d.tokens
        where d.documentType = :documentType
        and (:afterId is null or d.aggregateId > :afterId)
        order by d.aggregateId
        """)
    List<SearchDocument> findReconciliationBatch(@Param("documentType") SearchDocumentType documentType,
                                                  @Param("afterId") UUID afterId,
                                                  Pageable pageable);
}

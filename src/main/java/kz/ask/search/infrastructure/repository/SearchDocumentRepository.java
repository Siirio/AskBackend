package kz.ask.search.infrastructure.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import kz.ask.search.domain.entity.SearchDocument;
import kz.ask.search.domain.enums.SearchDocumentType;
import kz.ask.shared.domain.enums.RecordStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SearchDocumentRepository extends JpaRepository<SearchDocument, UUID> {

    Optional<SearchDocument> findByProductOfferId(UUID productOfferId);

    Optional<SearchDocument> findByServiceBranchOfferId(UUID serviceBranchOfferId);

    Optional<SearchDocument> findByBrandDropId(UUID brandDropId);

    List<SearchDocument> findByStatus(RecordStatus status);

    @Query("""
            SELECT DISTINCT d FROM SearchDocument d
            LEFT JOIN FETCH d.business
            LEFT JOIN FETCH d.branch b
            LEFT JOIN FETCH b.city
            LEFT JOIN FETCH d.tokens
            WHERE d.status = kz.ask.shared.domain.enums.RecordStatus.ACTIVE
            AND d.documentType IN :documentTypes
            """)
    List<SearchDocument> findActiveCandidates(@Param("documentTypes") List<SearchDocumentType> documentTypes);

    @Query("""
            SELECT DISTINCT d FROM SearchDocument d
            LEFT JOIN d.tokens t
            LEFT JOIN d.business business
            LEFT JOIN d.branch branch
            WHERE d.status = kz.ask.shared.domain.enums.RecordStatus.ACTIVE
            AND d.documentType IN :documentTypes
            AND (:normalizedQuery IS NULL OR :normalizedQuery = ''
                 OR LOWER(d.title) LIKE CONCAT('%', :normalizedQuery, '%')
                 OR LOWER(d.summary) LIKE CONCAT('%', :normalizedQuery, '%')
                 OR LOWER(d.categoryLabel) LIKE CONCAT('%', :normalizedQuery, '%')
                 OR LOWER(d.sku) LIKE CONCAT('%', :normalizedQuery, '%')
                 OR LOWER(business.name) LIKE CONCAT('%', :normalizedQuery, '%')
                 OR LOWER(branch.name) LIKE CONCAT('%', :normalizedQuery, '%')
                 OR LOWER(t) LIKE CONCAT('%', :normalizedQuery, '%'))
            AND (:normalizedCategory IS NULL OR :normalizedCategory = ''
                 OR LOWER(d.categoryLabel) LIKE CONCAT('%', :normalizedCategory, '%'))
            """)
    Page<SearchDocument> search(@Param("documentTypes") List<SearchDocumentType> documentTypes,
                                 @Param("normalizedQuery") String normalizedQuery,
                                 @Param("normalizedCategory") String normalizedCategory,
                                 Pageable pageable);
}

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

    List<SearchDocument> findByStatus(RecordStatus status);

    @Query("""
            SELECT DISTINCT d FROM SearchDocument d LEFT JOIN d.tokens t
            WHERE d.status = kz.ask.shared.domain.enums.RecordStatus.ACTIVE
            AND d.documentType IN :documentTypes
            AND (:normalizedQuery IS NULL OR :normalizedQuery = ''
                 OR LOWER(d.title) LIKE CONCAT('%', :normalizedQuery, '%')
                 OR LOWER(t) LIKE CONCAT('%', :normalizedQuery, '%'))
            """)
    Page<SearchDocument> search(@Param("documentTypes") List<SearchDocumentType> documentTypes,
                                 @Param("normalizedQuery") String normalizedQuery,
                                 Pageable pageable);
}

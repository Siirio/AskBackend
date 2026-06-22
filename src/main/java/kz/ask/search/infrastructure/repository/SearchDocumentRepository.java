package kz.ask.search.infrastructure.repository;

import java.util.Optional;
import java.util.UUID;
import kz.ask.search.domain.entity.SearchDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SearchDocumentRepository extends JpaRepository<SearchDocument, UUID> {

    Optional<SearchDocument> findByProductOfferId(UUID productOfferId);
}

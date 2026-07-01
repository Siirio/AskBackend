package kz.ask.autodump.infrastructure.repository;

import java.util.List;
import java.util.UUID;
import kz.ask.autodump.domain.entity.AutodumpImportError;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AutodumpImportErrorRepository extends JpaRepository<AutodumpImportError, UUID> {

    List<AutodumpImportError> findByImportSessionIdOrderByCreatedAt(UUID importSessionId);
}

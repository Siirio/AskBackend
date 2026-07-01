package kz.ask.autodump.infrastructure.repository;

import java.util.List;
import java.util.UUID;
import kz.ask.autodump.domain.entity.AutodumpAuditEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AutodumpAuditEventRepository extends JpaRepository<AutodumpAuditEvent, UUID> {

    List<AutodumpAuditEvent> findByImportSessionIdOrderByCreatedAt(UUID importSessionId);
}

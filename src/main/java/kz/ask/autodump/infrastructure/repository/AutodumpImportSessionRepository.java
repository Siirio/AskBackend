package kz.ask.autodump.infrastructure.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import kz.ask.autodump.domain.entity.AutodumpImportSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AutodumpImportSessionRepository extends JpaRepository<AutodumpImportSession, UUID> {

    List<AutodumpImportSession> findByBranchIdOrderByCreatedAtDesc(UUID branchId);

    Optional<AutodumpImportSession> findByIdAndBranchId(UUID id, UUID branchId);
}

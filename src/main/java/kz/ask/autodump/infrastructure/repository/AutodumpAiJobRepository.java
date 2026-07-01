package kz.ask.autodump.infrastructure.repository;

import java.util.List;
import java.util.UUID;
import kz.ask.autodump.domain.entity.AutodumpAiJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AutodumpAiJobRepository extends JpaRepository<AutodumpAiJob, UUID> {

    List<AutodumpAiJob> findByImportSessionId(UUID importSessionId);
}

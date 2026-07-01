package kz.ask.autodump.infrastructure.repository;

import java.util.List;
import java.util.UUID;
import kz.ask.autodump.domain.entity.AutodumpRawInput;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AutodumpRawInputRepository extends JpaRepository<AutodumpRawInput, UUID> {

    List<AutodumpRawInput> findByImportSessionId(UUID importSessionId);
}

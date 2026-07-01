package kz.ask.autodump.infrastructure.repository;

import java.util.List;
import java.util.UUID;
import kz.ask.autodump.domain.entity.AutodumpDraftItem;
import kz.ask.autodump.domain.enums.DraftItemStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AutodumpDraftItemRepository extends JpaRepository<AutodumpDraftItem, UUID> {

    List<AutodumpDraftItem> findByImportSessionIdOrderByCreatedAt(UUID importSessionId);

    List<AutodumpDraftItem> findByImportSessionIdAndStatus(UUID importSessionId, DraftItemStatus status);

    List<AutodumpDraftItem> findByImportSessionIdAndIdIn(UUID importSessionId, List<UUID> ids);
}

package kz.ask.autodump.infrastructure.repository;

import java.util.List;
import java.util.UUID;
import kz.ask.autodump.domain.entity.AutodumpDraftAttribute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AutodumpDraftAttributeRepository extends JpaRepository<AutodumpDraftAttribute, UUID> {

    List<AutodumpDraftAttribute> findByDraftItemId(UUID draftItemId);

    void deleteAllByDraftItemIdIn(List<UUID> draftItemIds);
}

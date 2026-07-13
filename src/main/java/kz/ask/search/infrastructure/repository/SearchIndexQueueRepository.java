package kz.ask.search.infrastructure.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import kz.ask.search.domain.entity.SearchIndexQueue;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface SearchIndexQueueRepository extends JpaRepository<SearchIndexQueue, UUID> {

    Optional<SearchIndexQueue> findByEntityTypeAndEntityId(String entityType, UUID entityId);

    @Query("SELECT q FROM SearchIndexQueue q WHERE q.scheduledAt <= :now ORDER BY q.scheduledAt ASC")
    List<SearchIndexQueue> findScheduledBefore(Instant now, Pageable pageable);

    @Modifying
    @Transactional
    @Query("DELETE FROM SearchIndexQueue q WHERE q.entityType = :entityType AND q.entityId = :entityId")
    void deleteByEntityTypeAndEntityId(String entityType, UUID entityId);
}

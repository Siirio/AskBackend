package kz.ask.moderation.infrastructure.repository;

import java.util.List;
import java.util.UUID;
import kz.ask.moderation.domain.entity.BusinessVerificationHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BusinessVerificationHistoryRepository extends JpaRepository<BusinessVerificationHistory, UUID> {

    List<BusinessVerificationHistory> findByBusinessIdOrderByCreatedAtDesc(UUID businessId);
}

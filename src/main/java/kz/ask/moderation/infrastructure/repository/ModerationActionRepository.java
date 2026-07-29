package kz.ask.moderation.infrastructure.repository;

import java.util.List;
import java.util.UUID;
import kz.ask.moderation.domain.entity.ModerationAction;
import kz.ask.platform.domain.enums.ModerationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ModerationActionRepository extends JpaRepository<ModerationAction, UUID> {

    List<ModerationAction> findByModerationStatusOrderByCreatedAtAsc(ModerationStatus moderationStatus);

    List<ModerationAction> findByTargetTypeAndTargetIdOrderByCreatedAtDesc(
            kz.ask.platform.domain.enums.ModerationTargetType targetType, UUID targetId);

    Long countByModerationStatus(ModerationStatus moderationStatus);

    boolean existsByTargetTypeAndTargetIdAndModerationStatus(
            kz.ask.platform.domain.enums.ModerationTargetType targetType,
            UUID targetId,
            ModerationStatus moderationStatus);

    List<ModerationAction> findByTargetTypeAndTargetIdAndModerationStatus(
            kz.ask.platform.domain.enums.ModerationTargetType targetType,
            UUID targetId,
            ModerationStatus moderationStatus);
}

package kz.ask.platform.infrastructure.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import kz.ask.platform.domain.entity.PlatformMembership;
import kz.ask.platform.domain.enums.PlatformRole;
import kz.ask.shared.domain.enums.RecordStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlatformMembershipRepository extends JpaRepository<PlatformMembership, UUID> {

    @EntityGraph(attributePaths = {"permissions", "user"})
    Optional<PlatformMembership> findByUserIdAndStatus(UUID userId, RecordStatus status);

    @EntityGraph(attributePaths = {"permissions", "user"})
    List<PlatformMembership> findAllByOrderByCreatedAtDesc();

    long countByRoleAndStatus(PlatformRole role, RecordStatus status);
}

package kz.ask.platform.infrastructure.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import kz.ask.platform.domain.entity.PlatformMembership;
import kz.ask.identity.authorization.domain.enums.Role;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlatformMembershipRepository extends JpaRepository<PlatformMembership, UUID> {

    @EntityGraph(attributePaths = {"permissions", "user"})
    Optional<PlatformMembership> findByUserId(UUID userId);

    @EntityGraph(attributePaths = {"permissions", "user"})
    List<PlatformMembership> findAllByOrderByCreatedAtDesc();

    long countByRole(Role role);
}

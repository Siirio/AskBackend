package kz.ask.business.infrastructure.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import kz.ask.business.domain.entity.BusinessInvitation;
import kz.ask.business.domain.enums.BusinessInvitationStatus;
import kz.ask.business.domain.enums.BusinessMemberRole;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BusinessInvitationRepository extends JpaRepository<BusinessInvitation, UUID> {

    @EntityGraph(attributePaths = {"branchIds", "business", "invitedBy"})
    List<BusinessInvitation> findByBusinessIdOrderByCreatedAtDesc(UUID businessId);

    @EntityGraph(attributePaths = {"branchIds", "business", "invitedBy"})
    List<BusinessInvitation> findByInvitedEmailIgnoreCaseAndStatusOrderByCreatedAtDesc(
            String invitedEmail,
            BusinessInvitationStatus status);

    Boolean existsByBusinessIdAndInvitedEmailIgnoreCaseAndInvitedRoleAndStatus(
            UUID businessId,
            String invitedEmail,
            BusinessMemberRole invitedRole,
            BusinessInvitationStatus status);

    Long countByInvitedEmailIgnoreCaseAndStatusAndExpiresAtAfter(
            String invitedEmail,
            BusinessInvitationStatus status,
            java.time.Instant expiresAt);

    List<BusinessInvitation> findByInvitedEmailIgnoreCaseAndStatus(
            String invitedEmail,
            BusinessInvitationStatus status);

    @Override
    @EntityGraph(attributePaths = {"branchIds", "business", "invitedBy"})
    Optional<BusinessInvitation> findById(UUID id);
}

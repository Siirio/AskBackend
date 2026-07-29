package kz.ask.business.invitation.infrastructure.repository;

import java.util.UUID;
import kz.ask.business.invitation.domain.entity.BusinessInvitationAction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BusinessInvitationActionRepository extends JpaRepository<BusinessInvitationAction, UUID> {
}

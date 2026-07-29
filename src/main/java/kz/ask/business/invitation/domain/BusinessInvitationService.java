package kz.ask.business.invitation.domain;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import kz.ask.identity.authorization.domain.enums.Role;
import kz.ask.business.invitation.domain.dto.BusinessInvitationDto;
import kz.ask.business.invitation.domain.dto.CreatedBusinessInvitationDto;

public interface BusinessInvitationService {

    CreatedBusinessInvitationDto create(
            UUID businessId,
            String invitedEmail,
            Role invitedRole,
            UUID invitedByUserId,
            Set<UUID> branchIds);

    List<BusinessInvitationDto> findByBusiness(UUID businessId);

    List<BusinessInvitationDto> findPendingByEmail(String email);

    Long countPendingByEmail(String email);

    BusinessInvitationDto findById(UUID invitationId);

    BusinessInvitationDto accept(UUID invitationId, UUID acceptedByUserId);

    BusinessInvitationDto decline(UUID invitationId);

    BusinessInvitationDto revoke(UUID invitationId);
}

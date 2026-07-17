package kz.ask.business.domain;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import kz.ask.business.domain.dto.BusinessInvitationDto;
import kz.ask.business.domain.dto.CreatedBusinessInvitationDto;
import kz.ask.business.domain.enums.BusinessMemberRole;

public interface BusinessInvitationService {

    CreatedBusinessInvitationDto create(
            UUID businessId,
            String invitedEmail,
            BusinessMemberRole invitedRole,
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

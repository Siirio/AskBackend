package kz.ask.business.member.domain;

import java.util.List;
import java.util.UUID;
import kz.ask.business.member.domain.dto.BusinessMemberDto;
import kz.ask.identity.authorization.domain.enums.Role;

public interface BusinessMemberService {

    BusinessMemberDto createOwner(UUID businessId, UUID userId);

    BusinessMemberDto createMember(UUID businessId, UUID userId, Role role);

    BusinessMemberDto findByOwner(UUID userId);

    BusinessMemberDto findByUser(UUID userId);

    List<BusinessMemberDto> findActiveByUser(UUID userId);

    BusinessMemberDto findByBusinessAndUser(UUID businessId, UUID userId);

    Boolean isOwnerOfBusiness(UUID businessId, UUID userId);

    Boolean isManagerOrAboveOfBusiness(UUID businessId, UUID userId);

    Role getRoleInBusiness(UUID businessId, UUID userId);

    List<BusinessMemberDto> findByBusiness(UUID businessId);

    BusinessMemberDto findById(UUID membershipId);

    BusinessMemberDto updateRole(UUID membershipId, Role role);

    BusinessMemberDto deactivate(UUID membershipId);
}

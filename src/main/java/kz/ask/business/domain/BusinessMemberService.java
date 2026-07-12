package kz.ask.business.domain;

import java.util.UUID;
import kz.ask.business.domain.dto.BusinessMemberDto;
import kz.ask.business.domain.enums.BusinessMemberRole;

public interface BusinessMemberService {

    BusinessMemberDto createOwner(UUID businessId, UUID userId);

    BusinessMemberDto createMember(UUID businessId, UUID userId, BusinessMemberRole role);

    BusinessMemberDto findByOwner(UUID userId);

    BusinessMemberDto findByUser(UUID userId);

    BusinessMemberDto findByBusinessAndUser(UUID businessId, UUID userId);

    Boolean isOwnerOfBusiness(UUID businessId, UUID userId);

    Boolean isManagerOrAboveOfBusiness(UUID businessId, UUID userId);

    BusinessMemberRole getRoleInBusiness(UUID businessId, UUID userId);
}

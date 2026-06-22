package kz.ask.business.domain;

import java.util.UUID;
import kz.ask.business.domain.dto.BusinessMemberDto;

public interface BusinessMemberService {

    BusinessMemberDto createOwner(UUID businessId, UUID userId);

    BusinessMemberDto findByOwner(UUID userId);

    Boolean isOwnerOfBusiness(UUID businessId, UUID userId);
}

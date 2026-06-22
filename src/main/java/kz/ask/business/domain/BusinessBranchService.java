package kz.ask.business.domain;

import java.util.UUID;
import kz.ask.business.domain.dto.BusinessBranchDto;

public interface BusinessBranchService {

    BusinessBranchDto findById(UUID branchId);

    BusinessBranchDto findByBusinessAndId(UUID businessId, UUID branchId);

    BusinessBranchDto create(UUID businessId, UUID cityId, String name, String address, Boolean onlineOnly);

    BusinessBranchDto findFirstByBusinessId(UUID businessId);
}

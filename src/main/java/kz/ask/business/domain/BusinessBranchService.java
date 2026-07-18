package kz.ask.business.domain;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import kz.ask.business.domain.dto.BusinessBranchDto;

public interface BusinessBranchService {

    BusinessBranchDto findById(UUID branchId);

    BusinessBranchDto findByBusinessAndId(UUID businessId, UUID branchId);

    BusinessBranchDto create(UUID businessId, UUID cityId, String name, String address, String addressDetails, Boolean onlineOnly,
                             BigDecimal latitude, BigDecimal longitude);

    BusinessBranchDto findFirstByBusinessId(UUID businessId);

    List<BusinessBranchDto> listByBusiness(UUID businessId);

    BusinessBranchDto update(UUID branchId, String name, String address, String addressDetails, UUID cityId, Boolean onlineOnly,
                             BigDecimal latitude, BigDecimal longitude);
}

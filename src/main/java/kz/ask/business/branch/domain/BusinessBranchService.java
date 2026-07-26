package kz.ask.business.branch.domain;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import kz.ask.business.branch.domain.dto.BusinessBranchDto;
import kz.ask.business.branch.domain.dto.SpecialOpeningIntervalDto;
import kz.ask.business.branch.domain.dto.WeeklyOpeningIntervalDto;

public interface BusinessBranchService {

    BusinessBranchDto findById(UUID branchId);

    BusinessBranchDto findByBusinessAndId(UUID businessId, UUID branchId);

    Boolean allBelongToBusiness(UUID businessId, List<UUID> branchIds);

    BusinessBranchDto create(UUID businessId, UUID cityId, String name, String address, String addressDetails,
                             BigDecimal latitude, BigDecimal longitude,
                             String timeZoneId,
                             List<WeeklyOpeningIntervalDto> weeklyHours,
                             List<SpecialOpeningIntervalDto> specialHours);

    BusinessBranchDto findFirstByBusinessId(UUID businessId);

    List<BusinessBranchDto> listByBusiness(UUID businessId);

    BusinessBranchDto update(UUID branchId, String name, String address, String addressDetails, UUID cityId,
                             BigDecimal latitude, BigDecimal longitude,
                             String timeZoneId,
                             List<WeeklyOpeningIntervalDto> weeklyHours,
                             List<SpecialOpeningIntervalDto> specialHours);
}

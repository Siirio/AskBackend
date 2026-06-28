package kz.ask.business.application;

import java.util.List;
import java.util.UUID;
import kz.ask.business.api.dto.BranchResponse;
import kz.ask.business.api.dto.CreateBranchRequest;
import kz.ask.business.api.dto.UpdateBranchRequest;
import kz.ask.business.domain.BusinessBranchService;
import kz.ask.business.domain.BusinessService;
import kz.ask.business.domain.dto.BusinessBranchDto;
import kz.ask.identity.infrastructure.security.AskPrincipal;
import kz.ask.shared.error.ErrorCode;
import kz.ask.shared.error.ForbiddenException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class BranchManagementProcessor {

    private final BusinessService businessService;
    private final BusinessBranchService businessBranchService;

    @Transactional
    public BranchResponse createBranch(AskPrincipal principal, UUID businessId, CreateBranchRequest req) {
        verifyOwnerAccess(principal.getUserId(), businessId);
        BusinessBranchDto dto = businessBranchService.create(
                businessId, req.getCityId(), req.getName(), req.getAddress(),
                req.getOnlineOnly(), req.getLatitude(), req.getLongitude());
        return toResponse(dto);
    }

    public List<BranchResponse> listBranches(AskPrincipal principal, UUID businessId) {
        verifyOwnerAccess(principal.getUserId(), businessId);
        return businessBranchService.listByBusiness(businessId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public BranchResponse updateBranch(AskPrincipal principal, UUID businessId, UUID branchId, UpdateBranchRequest req) {
        verifyOwnerAccess(principal.getUserId(), businessId);
        BusinessBranchDto dto = businessBranchService.update(
                branchId, req.getName(), req.getAddress(), req.getCityId(),
                req.getOnlineOnly(), req.getLatitude(), req.getLongitude());
        return toResponse(dto);
    }

    private void verifyOwnerAccess(UUID userId, UUID businessId) {
        if (!businessService.isOwnerOfBusiness(businessId, userId)) {
            throw new ForbiddenException(ErrorCode.ACCESS_DENIED);
        }
    }

    private BranchResponse toResponse(BusinessBranchDto dto) {
        if (dto == null) return null;
        return BranchResponse.builder()
                .id(dto.getId())
                .businessId(dto.getBusinessId())
                .cityId(dto.getCityId())
                .cityName(dto.getCityName())
                .name(dto.getName())
                .address(dto.getAddress())
                .latitude(dto.getLatitude())
                .longitude(dto.getLongitude())
                .onlineOnly(dto.getOnlineOnly())
                .status(dto.getStatus())
                .build();
    }
}

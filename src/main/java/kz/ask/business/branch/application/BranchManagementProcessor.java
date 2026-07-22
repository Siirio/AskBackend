package kz.ask.business.branch.application;

import java.util.List;
import java.util.UUID;
import kz.ask.business.branch.api.dto.BranchResponse;
import kz.ask.business.branch.api.dto.CreateBranchRequest;
import kz.ask.business.branch.api.dto.UpdateBranchRequest;
import kz.ask.business.branch.domain.BusinessBranchService;
import kz.ask.business.member.domain.BusinessMemberService;
import kz.ask.business.core.domain.BusinessService;
import kz.ask.managedimport.domain.ManagedImportService;
import kz.ask.business.branch.domain.dto.BusinessBranchDto;
import kz.ask.identity.infrastructure.security.AskPrincipal;
import kz.ask.platform.domain.PlatformMembershipService;
import kz.ask.platform.domain.dto.PlatformMembershipDto;
import kz.ask.identity.authorization.domain.enums.Permission;
import kz.ask.shared.error.ErrorCode;
import kz.ask.shared.error.ForbiddenException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class BranchManagementProcessor {

    private final BusinessService businessService;
    private final BusinessMemberService businessMemberService;
    private final BusinessBranchService businessBranchService;
    private final PlatformMembershipService platformMembershipService;
    private final ManagedImportService managedImportService;

    @Transactional
    public BranchResponse createBranch(AskPrincipal principal, UUID businessId, CreateBranchRequest req) {
        verifyOwnerAccess(principal.getUserId(), businessId);
        BusinessBranchDto dto = businessBranchService.create(
                businessId, req.getCityId(), req.getName(), req.getAddress(), req.getAddressDetails(), req.getIsOnlineOnly(),
                req.getLatitude(), req.getLongitude());
        return toResponse(dto);
    }

    public List<BranchResponse> listBranches(AskPrincipal principal, UUID businessId) {
        verifyReadAccess(principal.getUserId(), businessId);
        return businessBranchService.listByBusiness(businessId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public BranchResponse updateBranch(AskPrincipal principal, UUID businessId, UUID branchId, UpdateBranchRequest req) {
        verifyOwnerAccess(principal.getUserId(), businessId);
        BusinessBranchDto dto = businessBranchService.update(
                branchId, req.getName(), req.getAddress(), req.getAddressDetails(), req.getCityId(), req.getIsOnlineOnly(),
                req.getLatitude(), req.getLongitude());
        return toResponse(dto);
    }

    private void verifyOwnerAccess(UUID userId, UUID businessId) {
        if (!businessService.isOwnerOfBusiness(businessId, userId)) {
            throw new ForbiddenException(ErrorCode.ACCESS_DENIED);
        }
    }

    private void verifyReadAccess(UUID userId, UUID businessId) {
        var member = businessMemberService.findByBusinessAndUser(businessId, userId);
        if (member == null && !hasPlatformItemsServicesAccess(userId, businessId)) {
            throw new ForbiddenException(ErrorCode.ACCESS_DENIED);
        }
    }

    private boolean hasPlatformItemsServicesAccess(UUID userId, UUID businessId) {
        PlatformMembershipDto membership = platformMembershipService.findActiveByUser(userId);
        if (membership == null
                || !membership.getPermissions().contains(Permission.EDIT_ITEMS_SERVICES_DURING_IMPORT)) {
            return false;
        }
        return Boolean.TRUE.equals(managedImportService.hasActiveGrant(businessId, userId));
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
                .addressDetails(dto.getAddressDetails())
                .isOnlineOnly(dto.getIsOnlineOnly())
                .latitude(dto.getLatitude())
                .longitude(dto.getLongitude())
                .build();
    }
}

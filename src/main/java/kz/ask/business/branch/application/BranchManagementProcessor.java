package kz.ask.business.branch.application;

import java.util.List;
import java.util.UUID;
import kz.ask.business.branch.api.dto.BranchOpeningSummaryResponse;
import kz.ask.business.branch.api.dto.BranchListResponse;
import kz.ask.business.branch.api.dto.BranchResponse;
import kz.ask.business.branch.api.dto.CreateBranchRequest;
import kz.ask.business.branch.api.dto.UpdateBranchRequest;
import kz.ask.business.branch.domain.BusinessBranchService;
import kz.ask.business.branch.domain.BranchOpeningHoursPolicy;
import kz.ask.business.member.domain.BusinessMemberService;
import kz.ask.business.core.domain.BusinessService;
import kz.ask.managedimport.domain.ManagedImportService;
import kz.ask.business.branch.domain.dto.BusinessBranchDto;
import kz.ask.business.branch.domain.dto.BranchOpeningSummary;
import kz.ask.business.core.domain.dto.BusinessDto;
import kz.ask.identity.infrastructure.security.AskPrincipal;
import kz.ask.shared.error.ErrorCode;
import kz.ask.shared.error.ForbiddenException;
import kz.ask.shared.error.NotFoundException;
import kz.ask.shared.error.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class BranchManagementProcessor {

    private final BusinessService businessService;
    private final BusinessMemberService businessMemberService;
    private final BusinessBranchService businessBranchService;
    private final BranchOpeningHoursPolicy branchOpeningHoursPolicy;
    private final ManagedImportService managedImportService;

    @Transactional
    public BranchResponse createBranch(AskPrincipal principal, UUID businessId, CreateBranchRequest req) {
        verifyManagerAccess(principal.getUserId(), businessId);
        verifyBranchCreationAllowed(businessId);
        BusinessBranchDto dto = businessBranchService.create(
                businessId, req.getCityId(), req.getName(), req.getAddress(), req.getAddressDetails(),
                req.getLatitude(), req.getLongitude(), req.getTimeZoneId(),
                req.getWeeklyHours(), req.getSpecialHours(), req.getPickupAvailable());
        return toResponse(dto);
    }

    public BranchListResponse listBranches(AskPrincipal principal, UUID businessId) {
        verifyReadAccess(principal.getUserId(), businessId);
        List<BranchResponse> branches = businessBranchService.listByBusiness(businessId)
                .stream()
                .map(this::toResponse)
                .toList();
        return BranchListResponse.builder().branches(branches).build();
    }

    @Transactional
    public BranchResponse updateBranch(AskPrincipal principal, UUID branchId, UpdateBranchRequest req) {
        BusinessBranchDto branch = businessBranchService.findById(branchId);
        if (branch == null) {
            throw new NotFoundException(ErrorCode.BRANCH_NOT_FOUND);
        }
        verifyManagerAccess(principal.getUserId(), branch.getBusinessId());
        BusinessBranchDto dto = businessBranchService.update(
                branchId, req.getName(), req.getAddress(), req.getAddressDetails(), req.getCityId(),
                req.getLatitude(), req.getLongitude(), req.getTimeZoneId(),
                req.getWeeklyHours(), req.getSpecialHours(), req.getPickupAvailable());
        return toResponse(dto);
    }

    private void verifyBranchCreationAllowed(UUID businessId) {
        BusinessDto business = businessService.findById(businessId);
        if (Boolean.TRUE.equals(business.getOnlineOnly())) {
            throw new ValidationException(ErrorCode.BRANCH_NOT_ALLOWED_ONLINE_ONLY);
        }
    }

    private void verifyManagerAccess(UUID userId, UUID businessId) {
        if (!businessService.isManagerOrAboveOfBusiness(businessId, userId)) {
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
        return Boolean.TRUE.equals(managedImportService.hasActiveGrant(businessId, userId));
    }

    private BranchResponse toResponse(BusinessBranchDto dto) {
        if (dto == null) return null;
        BranchOpeningSummary summary = branchOpeningHoursPolicy.evaluate(dto);
        return BranchResponse.builder()
                .id(dto.getId())
                .businessId(dto.getBusinessId())
                .cityId(dto.getCityId())
                .cityName(dto.getCityName())
                .name(dto.getName())
                .address(dto.getAddress())
                .addressDetails(dto.getAddressDetails())
                .latitude(dto.getLatitude())
                .longitude(dto.getLongitude())
                .timeZoneId(dto.getTimeZoneId())
                .weeklyHours(dto.getWeeklyHours())
                .specialHours(dto.getSpecialHours())
                .openingSummary(toOpeningResponse(summary))
                .pickupAvailable(dto.getPickupAvailable())
                .build();
    }

    private BranchOpeningSummaryResponse toOpeningResponse(BranchOpeningSummary summary) {
        if (summary == null) return null;
        return BranchOpeningSummaryResponse.builder()
                .state(summary.getState())
                .timeZoneId(summary.getTimeZoneId())
                .evaluatedAt(summary.getEvaluatedAt())
                .nextOpensAt(summary.getNextOpensAt())
                .nextClosesAt(summary.getNextClosesAt())
                .build();
    }
}

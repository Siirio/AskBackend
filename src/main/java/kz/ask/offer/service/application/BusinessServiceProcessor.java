package kz.ask.offer.service.application;

import java.util.UUID;
import kz.ask.business.domain.BranchMemberService;
import kz.ask.business.domain.BusinessBranchService;
import kz.ask.business.domain.BusinessService;
import kz.ask.business.domain.dto.BusinessBranchDto;
import kz.ask.business.domain.enums.CatalogScope;
import kz.ask.identity.infrastructure.security.AskPrincipal;
import kz.ask.managedimport.domain.ManagedImportService;
import kz.ask.platform.domain.PlatformMembershipService;
import kz.ask.platform.domain.dto.PlatformMembershipDto;
import kz.ask.platform.domain.enums.PlatformPermission;
import kz.ask.offer.service.api.dto.BusinessServiceCreateRequest;
import kz.ask.offer.service.api.dto.BusinessServiceListResponse;
import kz.ask.offer.service.api.dto.BusinessServiceRowResponse;
import kz.ask.offer.service.api.dto.BusinessServiceUpdateRequest;
import kz.ask.search.basic.domain.SearchOutboxService;
import kz.ask.search.basic.domain.enums.SearchAggregateType;
import kz.ask.search.basic.domain.enums.SearchEventType;
import kz.ask.offer.service.domain.ServiceService;
import kz.ask.shared.error.ErrorCode;
import kz.ask.shared.error.ForbiddenException;
import kz.ask.shared.error.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class BusinessServiceProcessor {

    private static final int MAX_PAGE_SIZE = 100;

    private final BusinessService businessService;
    private final BusinessBranchService businessBranchService;
    private final BranchMemberService branchMemberService;
    private final ServiceService serviceService;
    private final SearchOutboxService searchOutboxService;
    private final PlatformMembershipService platformMembershipService;
    private final ManagedImportService managedImportService;

    @Transactional(readOnly = true)
    public BusinessServiceListResponse listServices(AskPrincipal principal, UUID branchId, String categoryLabel,
                                                     Boolean active, String query, Integer page, Integer size) {
        BusinessBranchDto branch = requireBranch(branchId);
        requireAnyAccess(principal.getUserId(), branch);

        int safeSize = Math.min(Math.max(size == null ? 20 : size, 1), MAX_PAGE_SIZE);
        int safePage = Math.max(page == null ? 0 : page, 0);
        Page<ServiceOfferingDto> offers = serviceService.listOffers(
                branchId, categoryLabel, active, query, PageRequest.of(safePage, safeSize));

        return BusinessServiceListResponse.builder()
                .items(offers.getContent().stream().map(this::toRowResponse).toList())
                .page(offers.getNumber())
                .size(offers.getSize())
                .totalElements(offers.getTotalElements())
                .totalPages(offers.getTotalPages())
                .build();
    }

    @Transactional
    public BusinessServiceRowResponse createService(AskPrincipal principal, UUID branchId, BusinessServiceCreateRequest req) {
        BusinessBranchDto branch = requireBranch(branchId);
        requireAnyAccess(principal.getUserId(), branch);

        ServiceOfferingDto dto = serviceService.createService(branch.getBusinessId(), branchId, req);
        publishSearchEvent(dto);
        return toRowResponse(dto);
    }

    @Transactional
    public BusinessServiceRowResponse updateService(AskPrincipal principal, UUID branchId, UUID serviceOfferingId,
                                                     BusinessServiceUpdateRequest req) {
        BusinessBranchDto branch = requireBranch(branchId);
        requireAnyAccess(principal.getUserId(), branch);

        ServiceOfferingDto dto = serviceService.updateService(serviceOfferingId, req);
        publishSearchEvent(dto);
        return toRowResponse(dto);
    }

    private void publishSearchEvent(ServiceOfferingDto dto) {
        boolean live = Boolean.TRUE.equals(dto.getActive());
        searchOutboxService.publish(
                SearchAggregateType.SERVICE_BRANCH_OFFER,
                dto.getId(),
                live ? SearchEventType.UPSERT : SearchEventType.DELETE,
                1L);
    }

    private BusinessServiceRowResponse toRowResponse(ServiceOfferingDto dto) {
        return BusinessServiceRowResponse.builder()
                .serviceOfferingId(dto.getId())
                .branchId(dto.getBranchId())
                .categoryLabel(dto.getCategoryLabel())
                .name(dto.getName())
                .description(dto.getDescription())
                .serviceMode(dto.getServiceMode())
                .basePrice(dto.getBasePrice())
                .scheduleText(dto.getScheduleText())
                .active(dto.getActive())
                .updatedAt(dto.getUpdatedAt())
                .build();
    }

    private BusinessBranchDto requireBranch(UUID branchId) {
        BusinessBranchDto branch = businessBranchService.findById(branchId);
        if (branch == null) {
            throw new NotFoundException(ErrorCode.BRANCH_NOT_FOUND);
        }
        return branch;
    }

    private void requireAnyAccess(UUID userId, BusinessBranchDto branch) {
        if (businessService.isManagerOrAboveOfBusiness(branch.getBusinessId(), userId)) {
            return;
        }
        if (branchMemberService.isStaffOfBranch(branch.getId(), userId)) {
            return;
        }
        if (hasPlatformServiceAccess(userId, branch.getBusinessId())) {
            return;
        }
        throw new ForbiddenException(ErrorCode.ACCESS_DENIED);
    }

    private boolean hasPlatformServiceAccess(UUID userId, UUID businessId) {
        PlatformMembershipDto membership = platformMembershipService.findActiveByUser(userId);
        if (membership == null
                || !membership.getPermissions().contains(PlatformPermission.EDIT_CATALOG_DURING_IMPORT)) {
            return false;
        }
        CatalogScope scope = managedImportService.activeScope(businessId, userId);
        return scope == CatalogScope.SERVICES || scope == CatalogScope.BOTH;
    }
}

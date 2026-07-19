package kz.ask.service.application;

import java.util.UUID;
import kz.ask.business.domain.BranchMemberService;
import kz.ask.business.domain.BusinessBranchService;
import kz.ask.business.domain.BusinessService;
import kz.ask.business.domain.CategoryService;
import kz.ask.business.domain.dto.BusinessBranchDto;
import kz.ask.catalog.domain.CatalogCapabilityService;
import kz.ask.identity.infrastructure.security.AskPrincipal;
import kz.ask.service.api.dto.BusinessServiceCreateRequest;
import kz.ask.service.api.dto.BusinessServiceListResponse;
import kz.ask.service.api.dto.BusinessServiceRowResponse;
import kz.ask.service.api.dto.BusinessServiceUpdateRequest;
import kz.ask.search.domain.SearchOutboxService;
import kz.ask.search.domain.enums.SearchAggregateType;
import kz.ask.search.domain.enums.SearchEventType;
import kz.ask.service.domain.ServiceService;
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
    private final CategoryService categoryService;
    private final ServiceService serviceService;
    private final SearchOutboxService searchOutboxService;
    private final CatalogCapabilityService catalogCapabilityService;

    @Transactional(readOnly = true)
    public BusinessServiceListResponse listServices(AskPrincipal principal, UUID branchId, UUID categoryId,
                                                     Boolean active, String query, Integer page, Integer size) {
        BusinessBranchDto branch = requireBranch(branchId);
        requireAnyAccess(principal.getUserId(), branch);

        int safeSize = Math.min(Math.max(size == null ? 20 : size, 1), MAX_PAGE_SIZE);
        int safePage = Math.max(page == null ? 0 : page, 0);
        Page<ServiceBranchOfferDto> offers = serviceService.listOffers(
                branchId, categoryId, active, query, PageRequest.of(safePage, safeSize));

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

        categoryService.requireActiveCategory(req.getCategoryId());
        ServiceBranchOfferDto dto = serviceService.createService(branch.getBusinessId(), branchId, req);
        publishSearchEvent(dto);
        return toRowResponse(dto);
    }

    @Transactional
    public BusinessServiceRowResponse updateService(AskPrincipal principal, UUID branchId, UUID serviceOfferingId,
                                                     BusinessServiceUpdateRequest req) {
        BusinessBranchDto branch = requireBranch(branchId);
        requireAnyAccess(principal.getUserId(), branch);

        if (req.getCategoryId() != null) {
            categoryService.requireActiveCategory(req.getCategoryId());
        }
        ServiceBranchOfferDto dto = serviceService.updateService(serviceOfferingId, branchId, req);
        publishSearchEvent(dto);
        return toRowResponse(dto);
    }

    private void publishSearchEvent(ServiceBranchOfferDto dto) {
        boolean live = Boolean.TRUE.equals(dto.getActive()) && "ACTIVE".equals(dto.getStatus());
        searchOutboxService.publish(
                SearchAggregateType.SERVICE_BRANCH_OFFER,
                dto.getServiceBranchOfferId(),
                live ? SearchEventType.UPSERT : SearchEventType.DELETE,
                dto.getSearchVersion());
    }

    private BusinessServiceRowResponse toRowResponse(ServiceBranchOfferDto dto) {
        return BusinessServiceRowResponse.builder()
                .serviceOfferingId(dto.getServiceOfferingId())
                .serviceBranchOfferId(dto.getServiceBranchOfferId())
                .branchId(dto.getBranchId())
                .categoryId(dto.getCategoryId())
                .categoryLabel(dto.getCategoryLabel())
                .name(dto.getName())
                .description(dto.getDescription())
                .basePrice(dto.getBasePrice())
                .scheduleText(dto.getScheduleText())
                .active(dto.getActive())
                .imageUrl(dto.getImageUrl())
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
        if (Boolean.TRUE.equals(catalogCapabilityService.hasPlatformServiceAccess(
                userId, branch.getBusinessId()))) {
            return;
        }
        throw new ForbiddenException(ErrorCode.ACCESS_DENIED);
    }
}

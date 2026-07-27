package kz.ask.offer.service.application;

import java.util.UUID;
import kz.ask.business.branch.domain.BusinessBranchService;
import kz.ask.business.branch.domain.dto.BusinessBranchDto;
import kz.ask.business.core.domain.BusinessService;
import kz.ask.business.core.domain.enums.BusinessScope;
import kz.ask.business.member.domain.BranchMemberService;
import kz.ask.identity.infrastructure.security.AskPrincipal;
import kz.ask.managedimport.domain.ManagedImportService;
import kz.ask.offer.service.api.dto.BusinessServiceCreateRequest;
import kz.ask.offer.service.api.dto.BusinessServiceListResponse;
import kz.ask.offer.service.api.dto.BusinessServiceRowResponse;
import kz.ask.offer.service.api.dto.BusinessServiceUpdateRequest;
import kz.ask.offer.service.domain.ServiceService;
import kz.ask.search.basic.application.SearchProjectionComposer;
import kz.ask.search.basic.domain.SearchDocumentService;
import kz.ask.search.basic.domain.SearchOutboxService;
import kz.ask.search.basic.domain.dto.SearchDocumentDto;
import kz.ask.search.basic.domain.enums.SearchAggregateType;
import kz.ask.search.basic.domain.enums.SearchDocumentType;
import kz.ask.search.basic.domain.enums.SearchEventType;
import kz.ask.shared.error.ErrorCode;
import kz.ask.shared.error.ForbiddenException;
import kz.ask.shared.error.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class BusinessServiceProcessor {

    private static final int MAX_PAGE_SIZE = 100;
    private static final String CREATED_AT_FIELD = "createdAt";

    private final BusinessService businessService;
    private final BusinessBranchService businessBranchService;
    private final BranchMemberService branchMemberService;
    private final ServiceService serviceService;
    private final SearchOutboxService searchOutboxService;
    private final SearchDocumentService searchDocumentService;
    private final SearchProjectionComposer searchProjectionComposer;
    private final ManagedImportService managedImportService;

    @Transactional(readOnly = true)
    public BusinessServiceListResponse listServices(AskPrincipal principal, UUID businessId, UUID branchId,
                                                     String categoryName, Boolean active, String query,
                                                     Integer page, Integer size) {
        requireAnyAccess(principal.getUserId(), businessId, branchId);
        Page<ServiceOfferingDto> offers = serviceService.listOffers(
                businessId, branchId, categoryName, active, query,
                PageRequest.of(
                        Math.max(page == null ? 0 : page, 0),
                        Math.min(Math.max(size == null ? 20 : size, 1), MAX_PAGE_SIZE),
                        Sort.by(Sort.Direction.DESC, CREATED_AT_FIELD)));
        return BusinessServiceListResponse.builder()
                .items(offers.getContent().stream().map(this::toRowResponse).toList())
                .page(offers.getNumber())
                .size(offers.getSize())
                .totalElements(offers.getTotalElements())
                .totalPages(offers.getTotalPages())
                .build();
    }

    @Transactional
    public BusinessServiceRowResponse createService(AskPrincipal principal, UUID businessId,
                                                     BusinessServiceCreateRequest req) {
        requireAnyAccess(principal.getUserId(), businessId, req.getBranchId());
        ServiceOfferingDto dto = serviceService.createService(businessId, req);
        syncSearchProjection(dto);
        return toRowResponse(dto);
    }

    @Transactional
    public BusinessServiceRowResponse updateService(AskPrincipal principal, UUID businessId,
                                                     UUID serviceOfferingId, BusinessServiceUpdateRequest req) {
        ServiceOfferingDto current = serviceService.findById(businessId, serviceOfferingId);
        requireAnyAccess(principal.getUserId(), businessId,
                req.getBranchId() != null ? req.getBranchId() : current.getBranchId());
        ServiceOfferingDto updated = serviceService.updateService(businessId, serviceOfferingId, req);
        syncSearchProjection(updated);
        return toRowResponse(updated);
    }

    @Transactional
    public void deleteService(AskPrincipal principal, UUID businessId, UUID serviceOfferingId) {
        ServiceOfferingDto current = serviceService.findById(businessId, serviceOfferingId);
        requireAnyAccess(principal.getUserId(), businessId, current.getBranchId());
        Long version = searchDocumentService.delete(SearchDocumentType.SERVICE, serviceOfferingId);
        serviceService.deleteService(businessId, serviceOfferingId);
        searchOutboxService.publish(SearchAggregateType.SERVICE, serviceOfferingId,
                SearchEventType.DELETE, version);
    }

    private void syncSearchProjection(ServiceOfferingDto dto) {
        boolean searchable = Boolean.TRUE.equals(dto.getIsActive());
        if (searchable) {
            kz.ask.business.core.domain.dto.BusinessDto business = businessService.findById(dto.getBusinessId());
            BusinessBranchDto branch = dto.getBranchId() == null
                    ? null : businessBranchService.findByBusinessAndId(dto.getBusinessId(), dto.getBranchId());
            SearchDocumentDto projection = searchProjectionComposer.composeService(
                    dto.getId(),
                    dto.getBusinessId(),
                    dto.getBranchId(),
                    dto.getName(),
                    dto.getDescription(),
                    dto.getCategoryLabel(),
                    business.getName(),
                    branch == null ? null : branch.getName(),
                    dto.getBasePrice(),
                    business.getCurrency(),
                    dto.getAttributes(),
                    branch == null ? null : branch.getLatitude(),
                    branch == null ? null : branch.getLongitude());
            Long version = searchDocumentService.upsert(projection);
            searchOutboxService.publish(SearchAggregateType.SERVICE, dto.getId(),
                    SearchEventType.UPSERT, version);
        } else {
            Long version = searchDocumentService.delete(SearchDocumentType.SERVICE, dto.getId());
            searchOutboxService.publish(SearchAggregateType.SERVICE, dto.getId(),
                    SearchEventType.DELETE, version);
        }
    }

    private BusinessServiceRowResponse toRowResponse(ServiceOfferingDto dto) {
        return BusinessServiceRowResponse.builder()
                .serviceOfferingId(dto.getId())
                .branchId(dto.getBranchId())
                .categoryId(dto.getCategoryId())
                .categoryLabel(dto.getCategoryLabel())
                .name(dto.getName())
                .description(dto.getDescription())
                .serviceMode(dto.getServiceMode())
                .basePrice(dto.getBasePrice())
                .scheduleText(dto.getScheduleText())
                .attributes(dto.getAttributes())
                .isActive(dto.getIsActive())
                .updatedAt(dto.getUpdatedAt())
                .build();
    }

    private void requireAnyAccess(UUID userId, UUID businessId, UUID branchId) {
        if (branchId != null && businessBranchService.findByBusinessAndId(businessId, branchId) == null) {
            throw new NotFoundException(ErrorCode.BRANCH_NOT_FOUND);
        }
        if (businessService.isManagerOrAboveOfBusiness(businessId, userId)) return;
        if (branchId != null && branchMemberService.isStaffOfBranch(branchId, userId)) return;
        if (hasPlatformServiceAccess(userId, businessId)) return;
        throw new ForbiddenException(ErrorCode.ACCESS_DENIED);
    }

    private boolean hasPlatformServiceAccess(UUID userId, UUID businessId) {
        BusinessScope scope = managedImportService.activeScope(businessId, userId);
        return scope == BusinessScope.SERVICE || scope == BusinessScope.BOTH;
    }
}

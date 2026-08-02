package kz.ask.offer.service.application;

import java.util.List;
import java.util.UUID;
import kz.ask.business.branch.domain.BusinessBranchService;
import kz.ask.business.branch.domain.dto.BusinessBranchDto;
import kz.ask.business.core.domain.BusinessService;
import kz.ask.business.core.domain.enums.BusinessScope;
import kz.ask.business.member.domain.BranchMemberService;
import kz.ask.identity.infrastructure.security.AskPrincipal;
import kz.ask.managedimport.domain.ManagedImportService;
import kz.ask.moderation.application.ModerationProcessor;
import kz.ask.moderation.domain.ModerationAssessment;
import kz.ask.moderation.domain.ModerationKeywords;
import kz.ask.offer.service.api.dto.BusinessServiceCreateRequest;
import kz.ask.offer.service.api.dto.BusinessServiceListResponse;
import kz.ask.offer.service.api.dto.BusinessServiceRowResponse;
import kz.ask.offer.service.api.dto.BusinessServiceUpdateRequest;
import kz.ask.offer.service.domain.ServiceService;
import kz.ask.offer.service.domain.entity.Service;
import kz.ask.offer.service.infrastructure.repository.ServiceOfferingRepository;
import kz.ask.offer.media.CatalogImageMutation;
import kz.ask.platform.domain.enums.ModerationTargetType;
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
import org.springframework.web.multipart.MultipartFile;

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
    private final ModerationProcessor moderationProcessor;
    private final ServiceOfferingRepository serviceOfferingRepository;
    private final CatalogImageMutation catalogImageMutation;

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
        ModerationAssessment assessment = assess(
                req.getName(),
                req.getDescription(),
                req.getCategoryName(),
                req.getScheduleText(),
                req.getAttributes());
        if (assessment.requiresAction()) {
            req.setIsActive(Boolean.FALSE);
        }
        ServiceOfferingDto dto = serviceService.createService(businessId, req);
        moderationProcessor.flagAutomated(
                principal, ModerationTargetType.SERVICE, dto.getId(), assessment);
        syncSearchProjection(dto);
        return toRowResponse(dto);
    }

    @Transactional
    public BusinessServiceRowResponse updateService(AskPrincipal principal, UUID businessId,
                                                     UUID serviceOfferingId, BusinessServiceUpdateRequest req) {
        ServiceOfferingDto current = serviceService.findById(businessId, serviceOfferingId);
        requireAnyAccess(principal.getUserId(), businessId,
                req.getBranchId() != null ? req.getBranchId() : current.getBranchId());
        ModerationAssessment assessment = assess(
                req.getName() == null ? current.getName() : req.getName(),
                req.getDescription() == null ? current.getDescription() : req.getDescription(),
                req.getCategoryName() == null ? current.getCategoryLabel() : req.getCategoryName(),
                req.getScheduleText() == null ? current.getScheduleText() : req.getScheduleText(),
                req.getAttributes() == null ? current.getAttributes() : req.getAttributes());
        if (assessment.requiresAction()) {
            req.setIsActive(Boolean.FALSE);
        }
        ServiceOfferingDto updated = serviceService.updateService(businessId, serviceOfferingId, req);
        moderationProcessor.flagAutomated(
                principal, ModerationTargetType.SERVICE, updated.getId(), assessment);
        syncSearchProjection(updated);
        return toRowResponse(updated);
    }

    @Transactional
    public BusinessServiceRowResponse syncImages(
            AskPrincipal principal,
            UUID businessId,
            UUID serviceOfferingId,
            List<MultipartFile> files,
            List<String> order) {
        Service service = serviceOfferingRepository.findById(serviceOfferingId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.PRODUCT_NOT_FOUND));
        if (!businessId.equals(service.getBusiness().getId())) {
            throw new NotFoundException(ErrorCode.PRODUCT_NOT_FOUND);
        }
        UUID branchId = service.getBranch() == null ? null : service.getBranch().getId();
        requireAnyAccess(principal.getUserId(), businessId, branchId);
        CatalogImageMutation.Result result = catalogImageMutation.apply(
                List.copyOf(service.getImageFiles()), files, order);
        service.getImageFiles().clear();
        service.getImageFiles().addAll(result.storedNames());
        Service saved = serviceOfferingRepository.save(service);
        return toRowResponse(ServiceOfferingDto.builder()
                .id(saved.getId())
                .businessId(saved.getBusiness().getId())
                .branchId(saved.getBranch() == null ? null : saved.getBranch().getId())
                .categoryId(saved.getCategory().getId())
                .categoryLabel(saved.getCategoryLabel())
                .name(saved.getName())
                .description(saved.getDescription())
                .imageFiles(List.copyOf(saved.getImageFiles()))
                .serviceMode(saved.getServiceMode())
                .basePrice(saved.getBasePrice())
                .scheduleText(saved.getScheduleText())
                .attributes(saved.getAttributes())
                .isActive(saved.getIsActive())
                .updatedAt(saved.getUpdatedAt())
                .build());
    }

    @Transactional
    public void deleteService(AskPrincipal principal, UUID businessId, UUID serviceOfferingId) {
        ServiceOfferingDto current = serviceService.findById(businessId, serviceOfferingId);
        requireAnyAccess(principal.getUserId(), businessId, current.getBranchId());
        catalogImageMutation.deleteAfterCommit(current.getImageFiles() == null ? List.of() : current.getImageFiles());
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
                .images(catalogImageMutation.toResponses(dto.getImageFiles()))
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

    private ModerationAssessment assess(
            String name,
            String description,
            String category,
            String schedule,
            java.util.Map<String, Object> attributes) {
        return ModerationKeywords.assess(
                name,
                description,
                category,
                schedule,
                attributes == null ? null : attributes.toString());
    }
}

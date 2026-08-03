package kz.ask.offer.service.domain;

import java.util.UUID;
import kz.ask.business.branch.domain.entity.BusinessBranch;
import kz.ask.business.branch.infrastructure.repository.BusinessBranchRepository;
import kz.ask.business.category.domain.CategoryService;
import kz.ask.business.category.domain.entity.Category;
import kz.ask.business.category.domain.enums.CategoryType;
import kz.ask.business.core.domain.entity.Business;
import kz.ask.business.core.infrastructure.repository.BusinessRepository;
import kz.ask.offer.service.api.dto.BusinessServiceCreateRequest;
import kz.ask.offer.service.api.dto.BusinessServiceUpdateRequest;
import kz.ask.offer.service.application.ServiceOfferingDto;
import kz.ask.offer.service.domain.entity.Service;
import kz.ask.offer.service.infrastructure.repository.ServiceOfferingRepository;
import kz.ask.offer.purchase.infrastructure.mapper.PurchaseDestinationMapper;
import kz.ask.shared.error.ErrorCode;
import kz.ask.shared.error.NotFoundException;
import kz.ask.shared.error.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

@org.springframework.stereotype.Service
@RequiredArgsConstructor
public class ServiceServiceImpl implements ServiceService {

    private final ServiceOfferingRepository serviceOfferingRepository;
    private final BusinessRepository businessRepository;
    private final BusinessBranchRepository businessBranchRepository;
    private final CategoryService categoryService;
    private final PurchaseDestinationMapper purchaseDestinationMapper;

    @Override
    @Transactional(readOnly = true)
    public Page<ServiceOfferingDto> listOffers(UUID businessId, UUID branchId, String categoryName,
                                               Boolean active, String query, Pageable pageable) {
        String normalizedCategoryName = categoryName == null || categoryName.isBlank()
                ? "" : categoryName.trim().toLowerCase();
        String term = query == null || query.isBlank() ? "" : "%" + query.trim().toLowerCase() + "%";
        return serviceOfferingRepository.search(businessId, branchId, normalizedCategoryName, active, term, pageable)
                .map(this::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public ServiceOfferingDto findById(UUID businessId, UUID serviceOfferingId) {
        Service offering = serviceOfferingRepository.findById(serviceOfferingId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.PRODUCT_NOT_FOUND));
        if (!businessId.equals(offering.getBusiness().getId())) {
            throw new NotFoundException(ErrorCode.PRODUCT_NOT_FOUND);
        }
        return toDto(offering);
    }

    @Override
    @Transactional(readOnly = true)
    public Boolean allBelongToBusiness(UUID businessId, java.util.List<UUID> serviceIds) {
        return serviceIds.isEmpty()
                || serviceOfferingRepository.countByIdInAndBusinessId(serviceIds, businessId) == serviceIds.size();
    }

    @Override
    @Transactional
    public ServiceOfferingDto createService(UUID businessId, BusinessServiceCreateRequest req) {
        validateName(req.getName());
        Business business = businessRepository.getReferenceById(businessId);
        BusinessBranch branch = req.getBranchId() == null ? null : requireBranch(businessId, req.getBranchId());
        Category category = req.getCategoryId() != null
                ? categoryService.requireActiveCategory(req.getCategoryId(), CategoryType.SERVICE)
                : categoryService.resolveOrCreate(req.getCategoryName(), CategoryType.SERVICE);
        Service offering = new Service();
        offering.setBusiness(business);
        offering.setBranch(branch);
        offering.setCategory(category);
        applyCreateFields(offering, req);
        return toDto(serviceOfferingRepository.save(offering));
    }

    @Override
    @Transactional
    public ServiceOfferingDto updateService(UUID businessId, UUID serviceOfferingId,
                                            BusinessServiceUpdateRequest req) {
        Service offering = serviceOfferingRepository.findById(serviceOfferingId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.PRODUCT_NOT_FOUND));
        if (!businessId.equals(offering.getBusiness().getId())) {
            throw new NotFoundException(ErrorCode.PRODUCT_NOT_FOUND);
        }
        if (req.getName() != null && req.getName().isBlank()) validateName(req.getName());
        if (req.getCategoryId() != null || req.getCategoryName() != null) {
            Category category = req.getCategoryId() != null
                    ? categoryService.requireActiveCategory(req.getCategoryId(), CategoryType.SERVICE)
                    : categoryService.resolveOrCreate(req.getCategoryName(), CategoryType.SERVICE);
            offering.setCategory(category);
        }
        if (req.getBranchId() != null) {
            offering.setBranch(requireBranch(businessId, req.getBranchId()));
        }
        applyUpdateFields(offering, req);
        return toDto(serviceOfferingRepository.save(offering));
    }

    @Override
    @Transactional
    public void deleteService(UUID businessId, UUID serviceOfferingId) {
        Service offering = serviceOfferingRepository.findById(serviceOfferingId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.PRODUCT_NOT_FOUND));
        if (!businessId.equals(offering.getBusiness().getId())) {
            throw new NotFoundException(ErrorCode.PRODUCT_NOT_FOUND);
        }
        serviceOfferingRepository.delete(offering);
    }

    private void validateName(String name) {
        if (name == null || name.isBlank()) throw new ValidationException(ErrorCode.PRODUCT_NAME_BLANK);
    }

    private BusinessBranch requireBranch(UUID businessId, UUID branchId) {
        BusinessBranch branch = businessBranchRepository.findById(branchId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.BRANCH_NOT_FOUND));
        if (!businessId.equals(branch.getBusiness().getId())) {
            throw new NotFoundException(ErrorCode.BRANCH_NOT_FOUND);
        }
        return branch;
    }

    private void applyCreateFields(Service offering, BusinessServiceCreateRequest req) {
        offering.setName(req.getName().trim());
        offering.setDescription(req.getDescription());
        offering.getPurchaseDestinations().addAll(
                purchaseDestinationMapper.toEntities(req.getPurchaseDestinations()));
        offering.setServiceMode(req.getServiceMode());
        offering.setBasePrice(req.getBasePrice());
        offering.setScheduleText(req.getScheduleText());
        offering.setIsActive(req.getIsActive() != null ? req.getIsActive() : Boolean.TRUE);
        offering.setAttributes(req.getAttributes());
    }

    private void applyUpdateFields(Service offering, BusinessServiceUpdateRequest req) {
        if (req.getName() != null) offering.setName(req.getName().trim());
        if (req.getDescription() != null) offering.setDescription(req.getDescription());
        if (req.getPurchaseDestinations() != null) {
            offering.getPurchaseDestinations().clear();
            offering.getPurchaseDestinations().addAll(
                    purchaseDestinationMapper.toEntities(req.getPurchaseDestinations()));
        }
        if (req.getServiceMode() != null) offering.setServiceMode(req.getServiceMode());
        if (req.getBasePrice() != null) offering.setBasePrice(req.getBasePrice());
        if (req.getScheduleText() != null) offering.setScheduleText(req.getScheduleText());
        if (req.getIsActive() != null) offering.setIsActive(req.getIsActive());
        if (req.getAttributes() != null) offering.setAttributes(req.getAttributes());
    }

    private ServiceOfferingDto toDto(Service entity) {
        return ServiceOfferingDto.builder()
                .id(entity.getId())
                .businessId(entity.getBusiness().getId())
                .branchId(entity.getBranch() == null ? null : entity.getBranch().getId())
                .categoryId(entity.getCategory().getId())
                .categoryLabel(entity.getCategory().getName())
                .name(entity.getName())
                .description(entity.getDescription())
                .imageFiles(entity.getImageFiles() == null ? java.util.List.of() : new java.util.ArrayList<>(entity.getImageFiles()))
                .purchaseDestinations(purchaseDestinationMapper.toDtos(entity.getPurchaseDestinations()))
                .serviceMode(entity.getServiceMode())
                .basePrice(entity.getBasePrice())
                .scheduleText(entity.getScheduleText())
                .attributes(entity.getAttributes())
                .isActive(entity.getIsActive())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}

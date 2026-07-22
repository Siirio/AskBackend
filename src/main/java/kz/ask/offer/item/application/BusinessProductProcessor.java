package kz.ask.offer.item.application;

import java.util.UUID;
import kz.ask.business.domain.BranchMemberService;
import kz.ask.business.domain.BusinessBranchService;
import kz.ask.business.domain.BusinessService;
import kz.ask.business.domain.dto.BusinessBranchDto;
import kz.ask.business.domain.enums.CatalogScope;
import kz.ask.business.infrastructure.repository.BusinessBranchRepository;
import kz.ask.business.infrastructure.repository.BusinessRepository;
import kz.ask.managedimport.domain.ManagedImportService;
import kz.ask.moderation.domain.ModerationKeywords;
import kz.ask.offer.item.api.dto.BusinessProductCreateRequest;
import kz.ask.offer.item.api.dto.BusinessProductListResponse;
import kz.ask.offer.item.api.dto.BusinessProductRowResponse;
import kz.ask.offer.item.api.dto.BusinessProductUpdateRequest;
import kz.ask.offer.item.domain.entity.Item;
import kz.ask.offer.item.domain.enums.ProductModerationStatus;
import kz.ask.offer.item.infrastructure.repository.ProductRepository;
import kz.ask.identity.infrastructure.security.AskPrincipal;
import kz.ask.platform.domain.PlatformMembershipService;
import kz.ask.platform.domain.dto.PlatformMembershipDto;
import kz.ask.platform.domain.enums.PlatformPermission;
import kz.ask.search.basic.domain.SearchOutboxService;
import kz.ask.search.basic.domain.enums.SearchAggregateType;
import kz.ask.search.basic.domain.enums.SearchEventType;
import kz.ask.shared.error.ErrorCode;
import kz.ask.shared.error.ForbiddenException;
import kz.ask.shared.error.NotFoundException;
import kz.ask.shared.error.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class BusinessProductProcessor {

    private static final int MAX_PAGE_SIZE = 100;

    private final BusinessService businessService;
    private final BusinessBranchService businessBranchService;
    private final BranchMemberService branchMemberService;
    private final PlatformMembershipService platformMembershipService;
    private final ManagedImportService managedImportService;
    private final ProductRepository productRepository;
    private final BusinessRepository businessRepository;
    private final BusinessBranchRepository businessBranchRepository;
    private final SearchOutboxService searchOutboxService;

    @Transactional(readOnly = true)
    public BusinessProductListResponse listProducts(AskPrincipal principal, UUID branchId,
                                                      Boolean enabled, String query, Integer page, Integer size) {
        BusinessBranchDto branch = requireBranch(branchId);
        requireAnyAccess(principal.getUserId(), branch);

        int safeSize = Math.min(Math.max(size == null ? 20 : size, 1), MAX_PAGE_SIZE);
        int safePage = Math.max(page == null ? 0 : page, 0);
        Page<Item> products;
        if (query != null && !query.isBlank()) {
            String term = "%" + query.trim().toLowerCase() + "%";
            products = productRepository.searchByBranchAndName(branchId, term, PageRequest.of(safePage, safeSize));
        } else if (enabled != null) {
            products = productRepository.findByBranchIdAndEnabled(branchId, enabled, PageRequest.of(safePage, safeSize));
        } else {
            products = productRepository.findByBranchId(branchId, PageRequest.of(safePage, safeSize));
        }
        Page<ProductOfferDto> offerDtos = products.map(this::toOfferDto);

        return BusinessProductListResponse.builder()
                .items(offerDtos.getContent().stream().map(this::toRowResponse).toList())
                .page(offerDtos.getNumber())
                .size(offerDtos.getSize())
                .totalElements(offerDtos.getTotalElements())
                .totalPages(offerDtos.getTotalPages())
                .build();
    }

    @Transactional
    public BusinessProductRowResponse createProduct(AskPrincipal principal, UUID branchId, BusinessProductCreateRequest req) {
        BusinessBranchDto branch = requireBranch(branchId);
        requireAnyAccess(principal.getUserId(), branch);

        Item item = buildItem(branch.getBusinessId(), branchId, req);
        applyAutoModeration(item);
        Item saved = productRepository.save(item);
        ProductOfferDto dto = toOfferDto(saved);
        publishSearchEvent(dto);
        return toRowResponse(dto);
    }

    @Transactional
    public BusinessProductRowResponse updateProduct(AskPrincipal principal, UUID branchId, UUID productId,
                                                      BusinessProductUpdateRequest req) {
        BusinessBranchDto branch = requireBranch(branchId);
        requireAnyAccess(principal.getUserId(), branch);

        Item item = productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.PRODUCT_NOT_FOUND));
        if (req.getName() != null && req.getName().isBlank()) {
            throw new ValidationException(ErrorCode.PRODUCT_NAME_BLANK);
        }
        if (req.getName() != null) {
            item.setName(req.getName());
        }
        if (req.getCategoryLabel() != null) {
            item.setCategoryLabel(req.getCategoryLabel());
        }
        if (req.getDescription() != null) {
            item.setDescription(req.getDescription());
        }
        if (req.getTags() != null) {
            item.setTags(req.getTags());
        }
        if (req.getPrice() != null) {
            item.setPrice(req.getPrice());
        }
        if (req.getEnabled() != null) {
            item.setEnabled(req.getEnabled());
        }

        ProductOfferDto dto = toOfferDto(item);
        publishSearchEvent(dto);
        return toRowResponse(dto);
    }

    @Transactional
    public BusinessProductRowResponse deleteProduct(AskPrincipal principal, UUID branchId, UUID productId) {
        BusinessBranchDto branch = requireBranch(branchId);
        requireAnyAccess(principal.getUserId(), branch);

        Item item = productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.PRODUCT_NOT_FOUND));
        item.setEnabled(false);
        ProductOfferDto dto = toOfferDto(item);
        publishSearchEvent(dto);
        return toRowResponse(dto);
    }

    private Item buildItem(UUID businessId, UUID branchId, BusinessProductCreateRequest req) {
        Item item = new Item();
        item.setBusiness(businessRepository.getReferenceById(businessId));
        item.setBranch(branchId != null ? businessBranchRepository.getReferenceById(branchId) : null);
        item.setName(req.getName());
        item.setCategoryLabel(req.getCategoryLabel());
        item.setDescription(req.getDescription());
        item.setTags(req.getTags());
        item.setPrice(req.getPrice());
        item.setEnabled(req.getEnabled() != null ? req.getEnabled() : true);
        item.setModerationStatus(ProductModerationStatus.PENDING);
        return item;
    }

    private void applyAutoModeration(Item item) {
        String name = item.getName();
        String prohibited = ModerationKeywords.prohibitedMatch(name);
        if (prohibited != null) {
            item.setModerationStatus(ProductModerationStatus.REJECTED);
            item.setModerationNote("Auto-rejected: prohibited category — " + prohibited);
            return;
        }
        String toyWeapon = ModerationKeywords.toyWeaponMatch(name);
        if (toyWeapon != null) {
            item.setModerationStatus(ProductModerationStatus.PENDING);
            item.setModerationNote("Manual review: potential toy weapon — " + toyWeapon);
            return;
        }
        item.setModerationStatus(ProductModerationStatus.PENDING);
    }

    private void publishSearchEvent(ProductOfferDto dto) {
        boolean live = Boolean.TRUE.equals(dto.getEnabled());
        searchOutboxService.publish(
                SearchAggregateType.PRODUCT_OFFER,
                dto.getProductId(),
                live ? SearchEventType.UPSERT : SearchEventType.DELETE,
                null);
    }

    private ProductOfferDto toOfferDto(Item item) {
        return ProductOfferDto.builder()
            .productId(item.getId())
            .businessId(item.getBusiness() != null ? item.getBusiness().getId() : null)
            .branchId(item.getBranch() != null ? item.getBranch().getId() : null)
            .categoryLabel(item.getCategoryLabel())
            .name(item.getName())
            .description(item.getDescription())
            .tags(item.getTags())
            .price(item.getPrice())
            .enabled(item.getEnabled())
            .updatedAt(item.getUpdatedAt())
            .build();
    }

    private BusinessProductRowResponse toRowResponse(ProductOfferDto dto) {
        return BusinessProductRowResponse.builder()
                .productId(dto.getProductId())
                .branchId(dto.getBranchId())
                .categoryLabel(dto.getCategoryLabel())
                .name(dto.getName())
                .description(dto.getDescription())
                .tags(dto.getTags())
                .price(dto.getPrice())
                .enabled(dto.getEnabled())
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
        if (hasPlatformProductAccess(userId, branch.getBusinessId())) {
            return;
        }
        throw new ForbiddenException(ErrorCode.ACCESS_DENIED);
    }

    private boolean hasPlatformProductAccess(UUID userId, UUID businessId) {
        PlatformMembershipDto membership = platformMembershipService.findActiveByUser(userId);
        if (membership == null
                || !membership.getPermissions().contains(PlatformPermission.EDIT_CATALOG_DURING_IMPORT)) {
            return false;
        }
        CatalogScope scope = managedImportService.activeScope(businessId, userId);
        return scope == CatalogScope.PRODUCTS || scope == CatalogScope.BOTH;
    }
}

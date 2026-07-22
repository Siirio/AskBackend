package kz.ask.offer.item.application;

import java.time.Instant;
import java.util.UUID;
import kz.ask.business.branch.domain.BusinessBranchService;
import kz.ask.business.branch.domain.dto.BusinessBranchDto;
import kz.ask.business.branch.infrastructure.repository.BusinessBranchRepository;
import kz.ask.business.category.domain.CategoryService;
import kz.ask.business.category.domain.entity.Category;
import kz.ask.business.category.domain.enums.CategoryType;
import kz.ask.business.core.domain.BusinessService;
import kz.ask.business.core.domain.enums.BusinessScope;
import kz.ask.business.core.infrastructure.repository.BusinessRepository;
import kz.ask.business.member.domain.BranchMemberService;
import kz.ask.identity.authorization.domain.enums.Permission;
import kz.ask.identity.infrastructure.security.AskPrincipal;
import kz.ask.managedimport.domain.ManagedImportService;
import kz.ask.moderation.domain.ModerationKeywords;
import kz.ask.offer.item.api.dto.BusinessProductCreateRequest;
import kz.ask.offer.item.api.dto.BusinessProductListResponse;
import kz.ask.offer.item.api.dto.BusinessProductRowResponse;
import kz.ask.offer.item.api.dto.BusinessProductUpdateRequest;
import kz.ask.offer.item.domain.entity.Item;
import kz.ask.offer.item.domain.enums.ProductModerationStatus;
import kz.ask.offer.item.infrastructure.repository.ProductRepository;
import kz.ask.platform.domain.PlatformMembershipService;
import kz.ask.platform.domain.dto.PlatformMembershipDto;
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
    private final CategoryService categoryService;
    private final PlatformMembershipService platformMembershipService;
    private final ManagedImportService managedImportService;
    private final ProductRepository productRepository;
    private final BusinessRepository businessRepository;
    private final BusinessBranchRepository businessBranchRepository;
    private final SearchOutboxService searchOutboxService;

    @Transactional(readOnly = true)
    public BusinessProductListResponse listProducts(AskPrincipal principal, UUID businessId, UUID branchId,
                                                    Boolean enabled, String query, Integer page, Integer size) {
        requireAnyAccess(principal.getUserId(), businessId, branchId);
        int safeSize = Math.min(Math.max(size == null ? 20 : size, 1), MAX_PAGE_SIZE);
        int safePage = Math.max(page == null ? 0 : page, 0);
        Page<Item> items;
        PageRequest request = PageRequest.of(safePage, safeSize);
        if (branchId != null) {
            requireBranch(businessId, branchId);
            if (query != null && !query.isBlank()) {
                items = productRepository.searchByBranchAndName(branchId, term(query), request);
            } else if (enabled != null) {
                items = productRepository.findByBranchIdAndIsEnabled(branchId, enabled, request);
            } else {
                items = productRepository.findByBranchId(branchId, request);
            }
        } else if (query != null && !query.isBlank()) {
            items = productRepository.searchByBusinessAndName(businessId, term(query), request);
        } else if (enabled != null) {
            items = productRepository.findByBusinessIdAndIsEnabled(businessId, enabled, request);
        } else {
            items = productRepository.findByBusinessId(businessId, request);
        }
        Page<ProductOfferDto> offers = items.map(this::toOfferDto);
        return BusinessProductListResponse.builder()
                .items(offers.getContent().stream().map(this::toRowResponse).toList())
                .page(offers.getNumber())
                .size(offers.getSize())
                .totalElements(offers.getTotalElements())
                .totalPages(offers.getTotalPages())
                .build();
    }

    @Transactional
    public BusinessProductRowResponse createProduct(AskPrincipal principal, UUID businessId,
                                                    BusinessProductCreateRequest req) {
        UUID branchId = req.getBranchId();
        if (branchId != null) requireBranch(businessId, branchId);
        requireAnyAccess(principal.getUserId(), businessId, branchId);
        Item item = new Item();
        item.setBusiness(businessRepository.getReferenceById(businessId));
        item.setBranch(branchId == null ? null : businessBranchRepository.getReferenceById(branchId));
        item.setCategory(resolveCategory(req.getCategoryId(), req.getCategoryName()));
        item.setName(req.getName().trim());
        item.setDescription(req.getDescription());
        item.setTags(req.getTags());
        item.setPrice(req.getPrice());
        item.setIsEnabled(req.getIsEnabled() != null ? req.getIsEnabled() : Boolean.TRUE);
        item.setModerationStatus(ProductModerationStatus.PENDING);
        applyAutoModeration(item);
        ProductOfferDto dto = toOfferDto(productRepository.save(item));
        publishSearchEvent(dto);
        return toRowResponse(dto);
    }

    @Transactional
    public BusinessProductRowResponse updateProduct(AskPrincipal principal, UUID businessId, UUID productId,
                                                    BusinessProductUpdateRequest req) {
        Item item = productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.PRODUCT_NOT_FOUND));
        requireBusiness(item, businessId);
        UUID branchId = req.getBranchId() != null ? req.getBranchId()
                : item.getBranch() == null ? null : item.getBranch().getId();
        requireAnyAccess(principal.getUserId(), businessId, branchId);
        if (req.getName() != null && req.getName().isBlank()) {
            throw new ValidationException(ErrorCode.PRODUCT_NAME_BLANK);
        }
        if (req.getCategoryId() != null || req.getCategoryName() != null) {
            item.setCategory(resolveCategory(req.getCategoryId(), req.getCategoryName()));
        }
        if (req.getBranchId() != null) {
            requireBranch(businessId, req.getBranchId());
            item.setBranch(businessBranchRepository.getReferenceById(req.getBranchId()));
        }
        if (req.getName() != null) item.setName(req.getName().trim());
        if (req.getDescription() != null) item.setDescription(req.getDescription());
        if (req.getTags() != null) item.setTags(req.getTags());
        if (req.getPrice() != null) item.setPrice(req.getPrice());
        if (req.getIsEnabled() != null) item.setIsEnabled(req.getIsEnabled());
        ProductOfferDto dto = toOfferDto(productRepository.save(item));
        publishSearchEvent(dto);
        return toRowResponse(dto);
    }

    @Transactional
    public BusinessProductRowResponse deleteProduct(AskPrincipal principal, UUID businessId, UUID productId) {
        Item item = productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.PRODUCT_NOT_FOUND));
        requireBusiness(item, businessId);
        UUID branchId = item.getBranch() == null ? null : item.getBranch().getId();
        requireAnyAccess(principal.getUserId(), businessId, branchId);
        item.setIsEnabled(Boolean.FALSE);
        ProductOfferDto dto = toOfferDto(productRepository.save(item));
        publishSearchEvent(dto);
        return toRowResponse(dto);
    }

    private Category resolveCategory(UUID categoryId, String categoryName) {
        if (categoryId != null) return categoryService.requireActiveCategory(categoryId, CategoryType.ITEM);
        return categoryService.resolveOrCreate(categoryName, CategoryType.ITEM);
    }

    private void requireBusiness(Item item, UUID businessId) {
        if (item.getBusiness() == null || !businessId.equals(item.getBusiness().getId())) {
            throw new NotFoundException(ErrorCode.PRODUCT_NOT_FOUND);
        }
    }

    private BusinessBranchDto requireBranch(UUID businessId, UUID branchId) {
        BusinessBranchDto branch = businessBranchService.findByBusinessAndId(businessId, branchId);
        if (branch == null) throw new NotFoundException(ErrorCode.BRANCH_NOT_FOUND);
        return branch;
    }

    private void requireAnyAccess(UUID userId, UUID businessId, UUID branchId) {
        if (businessService.isManagerOrAboveOfBusiness(businessId, userId)) return;
        if (branchId != null && branchMemberService.isStaffOfBranch(branchId, userId)) return;
        if (hasPlatformProductAccess(userId, businessId)) return;
        throw new ForbiddenException(ErrorCode.ACCESS_DENIED);
    }

    private boolean hasPlatformProductAccess(UUID userId, UUID businessId) {
        PlatformMembershipDto membership = platformMembershipService.findActiveByUser(userId);
        if (membership == null || !membership.getPermissions().contains(Permission.EDIT_ITEMS_SERVICES_DURING_IMPORT)) {
            return false;
        }
        BusinessScope scope = managedImportService.activeScope(businessId, userId);
        return scope == BusinessScope.ITEM || scope == BusinessScope.BOTH;
    }

    private String term(String query) {
        return "%" + query.trim().toLowerCase() + "%";
    }

    private void applyAutoModeration(Item item) {
        String prohibited = ModerationKeywords.prohibitedMatch(item.getName());
        if (prohibited != null) {
            item.setModerationStatus(ProductModerationStatus.REJECTED);
            item.setModerationNote("Auto-rejected: prohibited category — " + prohibited);
            return;
        }
        item.setModerationStatus(ProductModerationStatus.PENDING);
    }

    private void publishSearchEvent(ProductOfferDto dto) {
        searchOutboxService.publish(SearchAggregateType.PRODUCT_OFFER, dto.getProductId(),
                Boolean.TRUE.equals(dto.getIsEnabled()) ? SearchEventType.UPSERT : SearchEventType.DELETE,
                Instant.now().toEpochMilli());
    }

    private ProductOfferDto toOfferDto(Item item) {
        return ProductOfferDto.builder()
                .productId(item.getId())
                .businessId(item.getBusiness().getId())
                .branchId(item.getBranch() == null ? null : item.getBranch().getId())
                .categoryId(item.getCategory().getId())
                .categoryLabel(item.getCategoryLabel())
                .name(item.getName())
                .description(item.getDescription())
                .tags(item.getTags())
                .price(item.getPrice())
                .isEnabled(item.getIsEnabled())
                .updatedAt(item.getUpdatedAt())
                .build();
    }

    private BusinessProductRowResponse toRowResponse(ProductOfferDto dto) {
        return BusinessProductRowResponse.builder()
                .productId(dto.getProductId())
                .branchId(dto.getBranchId())
                .categoryId(dto.getCategoryId())
                .categoryLabel(dto.getCategoryLabel())
                .name(dto.getName())
                .description(dto.getDescription())
                .tags(dto.getTags())
                .price(dto.getPrice())
                .isEnabled(dto.getIsEnabled())
                .updatedAt(dto.getUpdatedAt())
                .build();
    }
}

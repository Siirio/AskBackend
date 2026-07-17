package kz.ask.catalog.application;

import java.util.UUID;
import kz.ask.business.domain.BranchMemberService;
import kz.ask.business.domain.BusinessBranchService;
import kz.ask.business.domain.BusinessService;
import kz.ask.business.domain.CategoryService;
import kz.ask.business.domain.dto.BusinessBranchDto;
import kz.ask.catalog.api.dto.BusinessProductCreateRequest;
import kz.ask.catalog.api.dto.BusinessProductListResponse;
import kz.ask.catalog.api.dto.BusinessProductRowResponse;
import kz.ask.catalog.api.dto.BusinessProductUpdateRequest;
import kz.ask.catalog.domain.CatalogCapabilityService;
import kz.ask.catalog.domain.ProductService;
import kz.ask.identity.infrastructure.security.AskPrincipal;
import kz.ask.search.domain.SearchOutboxService;
import kz.ask.search.domain.enums.SearchAggregateType;
import kz.ask.search.domain.enums.SearchEventType;
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
public class BusinessProductProcessor {

    private static final int MAX_PAGE_SIZE = 100;

    private final BusinessService businessService;
    private final BusinessBranchService businessBranchService;
    private final BranchMemberService branchMemberService;
    private final CatalogCapabilityService catalogCapabilityService;
    private final CategoryService categoryService;
    private final ProductService productService;
    private final SearchOutboxService searchOutboxService;

    @Transactional(readOnly = true)
    public BusinessProductListResponse listProducts(AskPrincipal principal, UUID branchId, UUID categoryId,
                                                      Boolean enabled, String query, Integer page, Integer size) {
        BusinessBranchDto branch = requireBranch(branchId);
        requireAnyAccess(principal.getUserId(), branch);

        int safeSize = Math.min(Math.max(size == null ? 20 : size, 1), MAX_PAGE_SIZE);
        int safePage = Math.max(page == null ? 0 : page, 0);
        Page<ProductOfferDto> offers = productService.listOffers(
                branchId, categoryId, enabled, query, PageRequest.of(safePage, safeSize));

        return BusinessProductListResponse.builder()
                .items(offers.getContent().stream().map(this::toRowResponse).toList())
                .page(offers.getNumber())
                .size(offers.getSize())
                .totalElements(offers.getTotalElements())
                .totalPages(offers.getTotalPages())
                .build();
    }

    @Transactional
    public BusinessProductRowResponse createProduct(AskPrincipal principal, UUID branchId, BusinessProductCreateRequest req) {
        BusinessBranchDto branch = requireBranch(branchId);
        requireAnyAccess(principal.getUserId(), branch);

        categoryService.requireActiveCategory(req.getCategoryId());
        ProductOfferDto dto = productService.createProduct(branch.getBusinessId(), branchId, req);
        publishSearchEvent(dto);
        return toRowResponse(dto);
    }

    @Transactional
    public BusinessProductRowResponse updateProduct(AskPrincipal principal, UUID branchId, UUID productId,
                                                      BusinessProductUpdateRequest req) {
        BusinessBranchDto branch = requireBranch(branchId);
        requireAnyAccess(principal.getUserId(), branch);

        if (req.getCategoryId() != null) {
            categoryService.requireActiveCategory(req.getCategoryId());
        }
        ProductOfferDto dto = productService.updateProduct(productId, branchId, req);
        publishSearchEvent(dto);
        return toRowResponse(dto);
    }

    @Transactional
    public BusinessProductRowResponse deleteProduct(AskPrincipal principal, UUID branchId, UUID productId) {
        BusinessBranchDto branch = requireBranch(branchId);
        requireAnyAccess(principal.getUserId(), branch);

        ProductOfferDto dto = productService.deleteProduct(productId, branchId);
        publishSearchEvent(dto);
        return toRowResponse(dto);
    }

    private void publishSearchEvent(ProductOfferDto dto) {
        boolean live = Boolean.TRUE.equals(dto.getEnabled()) && "ACTIVE".equals(dto.getStatus());
        searchOutboxService.publish(
                SearchAggregateType.PRODUCT_OFFER,
                dto.getProductOfferId(),
                live ? SearchEventType.UPSERT : SearchEventType.DELETE,
                dto.getSearchVersion());
    }

    private BusinessProductRowResponse toRowResponse(ProductOfferDto dto) {
        return BusinessProductRowResponse.builder()
                .productId(dto.getProductId())
                .productOfferId(dto.getProductOfferId())
                .branchId(dto.getBranchId())
                .categoryId(dto.getCategoryId())
                .categoryLabel(dto.getCategoryLabel())
                .name(dto.getName())
                .description(dto.getDescription())
                .sku(dto.getSku())
                .tags(dto.getTags())
                .price(dto.getPrice())
                .enabled(dto.getEnabled())
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
        if (catalogCapabilityService.hasPlatformCatalogAccess(userId, branch.getBusinessId())) {
            return;
        }
        throw new ForbiddenException(ErrorCode.ACCESS_DENIED);
    }
}

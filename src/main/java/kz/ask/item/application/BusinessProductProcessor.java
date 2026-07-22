package kz.ask.item.application;

import java.util.UUID;
import kz.ask.business.domain.BranchMemberService;
import kz.ask.business.domain.BusinessBranchService;
import kz.ask.business.domain.BusinessService;
import kz.ask.business.domain.dto.BusinessBranchDto;
import kz.ask.item.api.dto.BusinessProductCreateRequest;
import kz.ask.item.api.dto.BusinessProductListResponse;
import kz.ask.item.api.dto.BusinessProductRowResponse;
import kz.ask.item.api.dto.BusinessProductUpdateRequest;
import kz.ask.item.domain.CatalogCapabilityService;
import kz.ask.item.domain.ProductService;
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
    private final ProductService productService;
    private final SearchOutboxService searchOutboxService;

    @Transactional(readOnly = true)
    public BusinessProductListResponse listProducts(AskPrincipal principal, UUID branchId,
                                                      Boolean enabled, String query, Integer page, Integer size) {
        BusinessBranchDto branch = requireBranch(branchId);
        requireAnyAccess(principal.getUserId(), branch);

        int safeSize = Math.min(Math.max(size == null ? 20 : size, 1), MAX_PAGE_SIZE);
        int safePage = Math.max(page == null ? 0 : page, 0);
        Page<ProductOfferDto> products = productService.listProducts(
                branchId, enabled, query, PageRequest.of(safePage, safeSize));

        return BusinessProductListResponse.builder()
                .items(products.getContent().stream().map(this::toRowResponse).toList())
                .page(products.getNumber())
                .size(products.getSize())
                .totalElements(products.getTotalElements())
                .totalPages(products.getTotalPages())
                .build();
    }

    @Transactional
    public BusinessProductRowResponse createProduct(AskPrincipal principal, UUID branchId, BusinessProductCreateRequest req) {
        BusinessBranchDto branch = requireBranch(branchId);
        requireAnyAccess(principal.getUserId(), branch);

        ProductOfferDto dto = productService.createProduct(branch.getBusinessId(), branchId, req);
        publishSearchEvent(dto);
        return toRowResponse(dto);
    }

    @Transactional
    public BusinessProductRowResponse updateProduct(AskPrincipal principal, UUID branchId, UUID productId,
                                                      BusinessProductUpdateRequest req) {
        BusinessBranchDto branch = requireBranch(branchId);
        requireAnyAccess(principal.getUserId(), branch);

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
        boolean live = Boolean.TRUE.equals(dto.getEnabled());
        searchOutboxService.publish(
                SearchAggregateType.PRODUCT_OFFER,
                dto.getProductId(),
                live ? SearchEventType.UPSERT : SearchEventType.DELETE,
                null);
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
        if (catalogCapabilityService.hasPlatformProductAccess(userId, branch.getBusinessId())) {
            return;
        }
        throw new ForbiddenException(ErrorCode.ACCESS_DENIED);
    }
}

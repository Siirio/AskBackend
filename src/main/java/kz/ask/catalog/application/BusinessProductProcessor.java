package kz.ask.catalog.application;

import java.util.UUID;
import kz.ask.business.domain.BusinessService;
import kz.ask.business.domain.CategoryService;
import kz.ask.business.domain.entity.BranchMember;
import kz.ask.business.domain.entity.BusinessBranch;
import kz.ask.business.domain.enums.BranchMemberRole;
import kz.ask.catalog.api.dto.BusinessProductCreateRequest;
import kz.ask.catalog.api.dto.BusinessProductListResponse;
import kz.ask.catalog.api.dto.BusinessProductRowResponse;
import kz.ask.catalog.api.dto.BusinessProductUpdateRequest;
import kz.ask.catalog.domain.ProductService;
import kz.ask.identity.infrastructure.security.AskPrincipal;
import kz.ask.search.domain.SearchDocumentService;
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
    private final CategoryService categoryService;
    private final ProductService productService;
    private final SearchDocumentService searchDocumentService;

    @Transactional(readOnly = true)
    public BusinessProductListResponse listProducts(AskPrincipal principal, UUID branchId, UUID categoryId,
                                                      Boolean enabled, String query, Integer page, Integer size) {
        BusinessBranch branch = requireBranch(branchId);
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
        BusinessBranch branch = requireBranch(branchId);
        requireManagerOrAbove(principal.getUserId(), branch, "create a product");

        categoryService.requireActiveCategory(req.getCategoryId());
        ProductOfferDto dto = productService.createProduct(branch.getBusiness().getId(), branchId, req);
        syncSearchDocument(dto);
        return toRowResponse(dto);
    }

    @Transactional
    public BusinessProductRowResponse updateProduct(AskPrincipal principal, UUID branchId, UUID productId,
                                                      BusinessProductUpdateRequest req) {
        BusinessBranch branch = requireBranch(branchId);
        if (req.hasOnlyEnabledField()) {
            requireAnyAccess(principal.getUserId(), branch);
        } else {
            requireManagerOrAbove(principal.getUserId(), branch, "edit product fields other than availability");
        }

        if (req.getCategoryId() != null) {
            categoryService.requireActiveCategory(req.getCategoryId());
        }
        ProductOfferDto dto = productService.updateProduct(productId, branchId, req);
        syncSearchDocument(dto);
        return toRowResponse(dto);
    }

    @Transactional
    public BusinessProductRowResponse deleteProduct(AskPrincipal principal, UUID branchId, UUID productId) {
        BusinessBranch branch = requireBranch(branchId);
        requireManagerOrAbove(principal.getUserId(), branch, "delete a product");

        ProductOfferDto dto = productService.deleteProduct(productId, branchId);
        syncSearchDocument(dto);
        return toRowResponse(dto);
    }

    private void syncSearchDocument(ProductOfferDto dto) {
        boolean live = Boolean.TRUE.equals(dto.getEnabled()) && "ACTIVE".equals(dto.getStatus());
        searchDocumentService.syncProductDocument(
                dto.getProductOfferId(), dto.getName(), dto.getDescription(), dto.getTags(), live);
    }

    private BusinessProductRowResponse toRowResponse(ProductOfferDto dto) {
        return BusinessProductRowResponse.builder()
                .productId(dto.getProductId())
                .productOfferId(dto.getProductOfferId())
                .branchId(dto.getBranchId())
                .categoryId(dto.getCategoryId())
                .name(dto.getName())
                .description(dto.getDescription())
                .sku(dto.getSku())
                .tags(dto.getTags())
                .price(dto.getPrice())
                .enabled(dto.getEnabled())
                .status(dto.getStatus())
                .updatedAt(dto.getUpdatedAt())
                .build();
    }

    private BusinessBranch requireBranch(UUID branchId) {
        BusinessBranch branch = businessService.findBranchById(branchId);
        if (branch == null) {
            throw new NotFoundException(ErrorCode.BRANCH_NOT_FOUND);
        }
        return branch;
    }

    private void requireAnyAccess(UUID userId, BusinessBranch branch) {
        if (isOwner(userId, branch) || resolveBranchRole(userId, branch.getId()) != null) {
            return;
        }
        throw new ForbiddenException(ErrorCode.ACCESS_DENIED);
    }

    private void requireManagerOrAbove(UUID userId, BusinessBranch branch, String action) {
        if (isOwner(userId, branch)) {
            return;
        }
        BranchMemberRole role = resolveBranchRole(userId, branch.getId());
        if (role == null) {
            throw new ForbiddenException(ErrorCode.ACCESS_DENIED);
        }
        if (role != BranchMemberRole.MANAGER) {
            throw new ForbiddenException(ErrorCode.OPERATOR_FORBIDDEN_ACTION, action);
        }
    }

    private boolean isOwner(UUID userId, BusinessBranch branch) {
        return businessService.isOwnerOfBusiness(branch.getBusiness().getId(), userId);
    }

    private BranchMemberRole resolveBranchRole(UUID userId, UUID branchId) {
        return businessService.findBranchMembers(branchId).stream()
                .filter(m -> m.getUser().getId().equals(userId))
                .map(BranchMember::getRole)
                .findFirst()
                .orElse(null);
    }
}

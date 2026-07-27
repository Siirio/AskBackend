package kz.ask.offer.item.application;

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
import kz.ask.identity.infrastructure.security.AskPrincipal;
import kz.ask.managedimport.domain.ManagedImportService;
import kz.ask.moderation.domain.ModerationKeywords;
import kz.ask.offer.item.api.dto.BusinessProductCreateRequest;
import kz.ask.offer.item.api.dto.BusinessProductListResponse;
import kz.ask.offer.item.api.dto.BusinessProductRowResponse;
import kz.ask.offer.item.api.dto.BusinessProductUpdateRequest;
import kz.ask.offer.item.domain.entity.Item;
import kz.ask.offer.item.domain.enums.ProductModerationStatus;
import kz.ask.offer.item.infrastructure.mapper.ItemMapper;
import kz.ask.offer.item.infrastructure.repository.ProductRepository;
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
import kz.ask.shared.error.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class BusinessProductProcessor {

    private static final int MAX_PAGE_SIZE = 100;
    private static final String CREATED_AT_FIELD = "createdAt";

    private final BusinessService businessService;
    private final BusinessBranchService businessBranchService;
    private final BranchMemberService branchMemberService;
    private final CategoryService categoryService;
    private final ManagedImportService managedImportService;
    private final ProductRepository productRepository;
    private final BusinessRepository businessRepository;
    private final BusinessBranchRepository businessBranchRepository;
    private final SearchOutboxService searchOutboxService;
    private final SearchDocumentService searchDocumentService;
    private final SearchProjectionComposer searchProjectionComposer;
    private final ItemMapper itemMapper;

    @Transactional(readOnly = true)
    public BusinessProductListResponse listProducts(AskPrincipal principal, UUID businessId, UUID branchId,
                                                    Boolean enabled, String query, Integer page, Integer size) {
        requireAnyAccess(principal.getUserId(), businessId, branchId);
        int safeSize = Math.min(Math.max(size == null ? 20 : size, 1), MAX_PAGE_SIZE);
        int safePage = Math.max(page == null ? 0 : page, 0);
        Page<Item> items;
        PageRequest request = PageRequest.of(
                safePage,
                safeSize,
                Sort.by(Sort.Direction.DESC, CREATED_AT_FIELD));
        if (branchId != null) {
            requireBranch(businessId, branchId);
            if (query != null && !query.isBlank()) {
                items = productRepository.searchByBranchAndName(branchId, term(query), request);
            } else if (enabled != null) {
                items = productRepository.findByBranchIdAndIsActive(branchId, enabled, request);
            } else {
                items = productRepository.findByBranchId(branchId, request);
            }
        } else if (query != null && !query.isBlank()) {
            items = productRepository.searchByBusinessAndName(businessId, term(query), request);
        } else if (enabled != null) {
            items = productRepository.findByBusinessIdAndIsActive(businessId, enabled, request);
        } else {
            items = productRepository.findByBusinessId(businessId, request);
        }
        Page<ProductOfferDto> offers = items.map(itemMapper::toProductOfferDto);
        return BusinessProductListResponse.builder()
                .items(offers.getContent().stream().map(itemMapper::toBusinessProductRowResponse).toList())
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
        itemMapper.applyCreateFields(item,
                businessRepository.getReferenceById(businessId),
                branchId == null ? null : businessBranchRepository.getReferenceById(branchId),
                resolveCategory(req.getCategoryId(), req.getCategoryName()),
                req);
        applyAutoModeration(item);
        Item saved = productRepository.save(item);
        ProductOfferDto dto = itemMapper.toProductOfferDto(saved);
        syncSearchProjection(saved, dto);
        return itemMapper.toBusinessProductRowResponse(dto);
    }

    @Transactional
    public BusinessProductRowResponse updateProduct(AskPrincipal principal, UUID itemId,
                                                    BusinessProductUpdateRequest req) {
        Item item = productRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.PRODUCT_NOT_FOUND));
        UUID businessId = item.getBusiness().getId();
        UUID branchId = req.getBranchId() != null ? req.getBranchId()
                : item.getBranch() == null ? null : item.getBranch().getId();
        requireAnyAccess(principal.getUserId(), businessId, branchId);
        if (req.getName() != null && req.getName().isBlank()) {
            throw new ValidationException(ErrorCode.PRODUCT_NAME_BLANK);
        }
        Category category = (req.getCategoryId() != null || req.getCategoryName() != null)
                ? resolveCategory(req.getCategoryId(), req.getCategoryName()) : null;
        kz.ask.business.branch.domain.entity.BusinessBranch branch = item.getBranch();
        if (req.getBranchId() != null) {
            requireBranch(businessId, req.getBranchId());
            branch = businessBranchRepository.getReferenceById(req.getBranchId());
        }
        itemMapper.applyUpdateFields(item, req, branch, category);
        Item saved = productRepository.save(item);
        ProductOfferDto dto = itemMapper.toProductOfferDto(saved);
        syncSearchProjection(saved, dto);
        return itemMapper.toBusinessProductRowResponse(dto);
    }

    @Transactional
    public void deleteProduct(AskPrincipal principal, UUID itemId) {
        Item item = productRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.PRODUCT_NOT_FOUND));
        UUID businessId = item.getBusiness().getId();
        UUID branchId = item.getBranch() == null ? null : item.getBranch().getId();
        requireAnyAccess(principal.getUserId(), businessId, branchId);
        UUID productId = item.getId();
        Long version = searchDocumentService.delete(SearchDocumentType.ITEM, productId);
        productRepository.delete(item);
        searchOutboxService.publish(SearchAggregateType.ITEM, productId,
                SearchEventType.DELETE, version);
    }

    private void syncSearchProjection(Item item, ProductOfferDto dto) {
        boolean searchable = Boolean.TRUE.equals(dto.getIsActive())
                && dto.getModerationStatus() == ProductModerationStatus.APPROVED;
        if (searchable) {
            SearchDocumentDto projection = searchProjectionComposer.composeItem(
                    item.getId(),
                    item.getBusiness().getId(),
                    item.getBranch() != null ? item.getBranch().getId() : null,
                    item.getName(),
                    item.getDescription(),
                    item.getCategoryLabel(),
                    item.getBusiness().getName(),
                    item.getBranch() != null ? item.getBranch().getName() : null,
                    item.getPrice(),
                    item.getBusiness().getCurrency(),
                    item.getTags(),
                    item.getAttributes(),
                    item.getBranch() != null ? item.getBranch().getLatitude() : null,
                    item.getBranch() != null ? item.getBranch().getLongitude() : null);
            Long version = searchDocumentService.upsert(projection);
            searchOutboxService.publish(SearchAggregateType.ITEM, item.getId(),
                    SearchEventType.UPSERT, version);
        } else {
            Long version = searchDocumentService.delete(SearchDocumentType.ITEM, item.getId());
            searchOutboxService.publish(SearchAggregateType.ITEM, item.getId(),
                    SearchEventType.DELETE, version);
        }
    }

    private Category resolveCategory(UUID categoryId, String categoryName) {
        if (categoryId != null) return categoryService.requireActiveCategory(categoryId, CategoryType.ITEM);
        return categoryService.resolveOrCreate(categoryName, CategoryType.ITEM);
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
        item.setModerationStatus(ProductModerationStatus.APPROVED);
    }
}

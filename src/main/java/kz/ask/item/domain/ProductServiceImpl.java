package kz.ask.item.domain;

import java.util.UUID;
import kz.ask.business.domain.entity.Business;
import kz.ask.business.domain.entity.BusinessBranch;
import kz.ask.business.infrastructure.repository.BusinessBranchRepository;
import kz.ask.business.infrastructure.repository.BusinessRepository;
import kz.ask.item.api.dto.BusinessProductCreateRequest;
import kz.ask.item.api.dto.BusinessProductUpdateRequest;
import kz.ask.item.application.ProductOfferDto;
import kz.ask.item.domain.entity.Item;
import kz.ask.item.domain.enums.ProductModerationStatus;
import kz.ask.item.infrastructure.repository.ProductRepository;
import kz.ask.moderation.domain.ModerationKeywords;
import kz.ask.shared.error.ErrorCode;
import kz.ask.shared.error.NotFoundException;
import kz.ask.shared.error.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final BusinessRepository businessRepository;
    private final BusinessBranchRepository businessBranchRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<ProductOfferDto> listProducts(UUID branchId, Boolean enabled, String query, Pageable pageable) {
        Page<Item> products;
        if (query != null && !query.isBlank()) {
            String term = "%" + query.trim().toLowerCase() + "%";
            products = productRepository.searchByBranchAndName(branchId, term, pageable);
        } else if (enabled != null) {
            products = productRepository.findByBranchIdAndEnabled(branchId, enabled, pageable);
        } else {
            products = productRepository.findByBranchId(branchId, pageable);
        }
        return products.map(this::toDto);
    }

    @Override
    @Transactional
    public ProductOfferDto createProduct(UUID businessId, UUID branchId, BusinessProductCreateRequest req) {
        Business businessRef = businessRepository.getReferenceById(businessId);
        BusinessBranch branchRef = (branchId != null)
                ? businessBranchRepository.getReferenceById(branchId)
                : null;

        Item item = new Item();
        item.setBusiness(businessRef);
        item.setBranch(branchRef);
        item.setName(req.getName());
        item.setCategoryLabel(req.getCategoryLabel());
        item.setDescription(req.getDescription());
        item.setTags(req.getTags());
        item.setPrice(req.getPrice());
        item.setEnabled(req.getEnabled() != null ? req.getEnabled() : true);
        item.setModerationStatus(ProductModerationStatus.PENDING);

        applyAutoModeration(item);
        Item saved = productRepository.save(item);
        return toDto(saved);
    }

    @Override
    @Transactional
    public ProductOfferDto updateProduct(UUID productId, UUID branchId, BusinessProductUpdateRequest req) {
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

        return toDto(item);
    }

    @Override
    @Transactional
    public ProductOfferDto deleteProduct(UUID productId, UUID branchId) {
        Item item = productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.PRODUCT_NOT_FOUND));
        item.setEnabled(false);
        return toDto(item);
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

    private ProductOfferDto toDto(Item item) {
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
}

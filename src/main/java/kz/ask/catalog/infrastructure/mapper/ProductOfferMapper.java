package kz.ask.catalog.infrastructure.mapper;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import kz.ask.business.domain.entity.Business;
import kz.ask.business.domain.entity.BusinessBranch;
import kz.ask.business.domain.entity.Category;
import kz.ask.catalog.api.dto.BusinessProductCreateRequest;
import kz.ask.catalog.api.dto.BusinessProductUpdateRequest;
import kz.ask.catalog.application.ProductOfferDto;
import kz.ask.catalog.domain.entity.Product;
import kz.ask.catalog.domain.entity.ProductOffer;
import kz.ask.shared.domain.enums.RecordStatus;
import org.springframework.stereotype.Component;

@Component
public class ProductOfferMapper {

    public Product toProductEntity(BusinessProductCreateRequest req, Business businessRef, Category categoryRef) {
        Product product = new Product();
        product.setBusiness(businessRef);
        product.setCategory(categoryRef);
        product.setName(req.getName().trim());
        product.setDescription(req.getDescription());
        product.setSku(normalizeSku(req.getSku()));
        product.setTags(normalizeTags(req.getTags()));
        product.setImageUrl(req.getImageUrl());
        product.setStatus(RecordStatus.ACTIVE);
        return product;
    }

    public ProductOffer toOfferEntity(BusinessProductCreateRequest req, Product product, BusinessBranch branchRef) {
        ProductOffer offer = new ProductOffer();
        offer.setProduct(product);
        offer.setBranch(branchRef);
        offer.setPrice(req.getPrice());
        offer.setEnabled(req.getEnabled() == null ? Boolean.TRUE : req.getEnabled());
        offer.setStatus(RecordStatus.ACTIVE);
        return offer;
    }

    public void applyUpdate(BusinessProductUpdateRequest req, Product product, ProductOffer offer, Category categoryRefOrNull) {
        if (categoryRefOrNull != null) {
            product.setCategory(categoryRefOrNull);
        }
        if (req.getName() != null) {
            product.setName(req.getName().trim());
        }
        if (req.getDescription() != null) {
            product.setDescription(req.getDescription());
        }
        if (req.getSku() != null) {
            product.setSku(normalizeSku(req.getSku()));
        }
        if (req.getTags() != null) {
            product.setTags(normalizeTags(req.getTags()));
        }
        if (req.getPrice() != null) {
            offer.setPrice(req.getPrice());
        }
        if (req.getEnabled() != null) {
            offer.setEnabled(req.getEnabled());
        }
        if (req.getImageUrl() != null) {
            product.setImageUrl(req.getImageUrl());
        }
    }

    public void markArchived(Product product, ProductOffer offer) {
        offer.setEnabled(Boolean.FALSE);
        offer.setStatus(RecordStatus.ARCHIVED);
        product.setStatus(RecordStatus.ARCHIVED);
    }

    public ProductOfferDto toDto(ProductOffer offer) {
        Product product = offer.getProduct();
        Category category = product.getCategory();
        return ProductOfferDto.builder()
                .productId(product.getId())
                .productOfferId(offer.getId())
                .searchVersion(offer.getSearchVersion())
                .businessId(product.getBusiness().getId())
                .branchId(offer.getBranch().getId())
                .categoryId(category != null ? category.getId() : null)
                .categoryLabel(category != null ? category.getName() : product.getCategoryLabel())
                .name(product.getName())
                .description(product.getDescription())
                .sku(product.getSku())
                .tags(new ArrayList<>(product.getTags()))
                .price(offer.getPrice())
                .enabled(offer.getEnabled())
                .status(offer.getStatus().name())
                .imageUrl(product.getImageUrl())
                .updatedAt(offer.getUpdatedAt())
                .build();
    }

    private String normalizeSku(String sku) {
        return (sku == null || sku.isBlank()) ? null : sku.trim();
    }

    private List<String> normalizeTags(List<String> tags) {
        if (tags == null) {
            return new ArrayList<>();
        }
        return tags.stream()
                .filter(t -> t != null && !t.isBlank())
                .map(t -> t.trim().toLowerCase())
                .distinct()
                .collect(Collectors.toList());
    }
}

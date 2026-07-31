package kz.ask.offer.item.infrastructure.mapper;

import java.util.UUID;
import kz.ask.business.branch.domain.entity.BusinessBranch;
import kz.ask.business.category.domain.entity.Category;
import kz.ask.business.core.domain.entity.Business;
import kz.ask.offer.item.api.dto.BusinessProductCreateRequest;
import kz.ask.offer.item.api.dto.BusinessProductRowResponse;
import kz.ask.offer.item.api.dto.BusinessProductUpdateRequest;
import kz.ask.moderation.api.dto.ProductModerationItemResponse;
import kz.ask.offer.item.application.ProductOfferDto;
import kz.ask.offer.item.domain.entity.Item;
import org.springframework.stereotype.Component;

@Component
public class ItemMapper {

    public ProductOfferDto toProductOfferDto(Item entity) {
        return ProductOfferDto.builder()
                .productId(entity.getId())
                .businessId(entity.getBusiness().getId())
                .branchId(entity.getBranch() == null ? null : entity.getBranch().getId())
                .categoryId(entity.getCategory().getId())
                .categoryLabel(entity.getCategoryLabel())
                .name(entity.getName())
                .description(entity.getDescription())
                .deepLink(entity.getDeepLink())
                .tags(entity.getTags() == null ? null : new java.util.ArrayList<>(entity.getTags()))
                .attributes(entity.getAttributes())
                .price(entity.getPrice())
                .isActive(entity.getIsActive())
                .moderationStatus(entity.getModerationStatus())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public BusinessProductRowResponse toBusinessProductRowResponse(ProductOfferDto dto) {
        return BusinessProductRowResponse.builder()
                .productId(dto.getProductId())
                .branchId(dto.getBranchId())
                .categoryId(dto.getCategoryId())
                .categoryLabel(dto.getCategoryLabel())
                .name(dto.getName())
                .description(dto.getDescription())
                .deepLink(dto.getDeepLink())
                .tags(dto.getTags())
                .attributes(dto.getAttributes())
                .price(dto.getPrice())
                .isActive(dto.getIsActive())
                .updatedAt(dto.getUpdatedAt())
                .build();
    }

    public void applyCreateFields(Item entity, Business business, BusinessBranch branch,
                                   Category category, BusinessProductCreateRequest req) {
        entity.setBusiness(business);
        entity.setBranch(branch);
        entity.setCategory(category);
        entity.setName(req.getName().trim());
        entity.setDescription(req.getDescription());
        entity.setDeepLink(req.getDeepLink());
        entity.setTags(req.getTags());
        entity.setAttributes(req.getAttributes());
        entity.setPrice(req.getPrice());
        entity.setIsActive(req.getIsActive() != null ? req.getIsActive() : Boolean.TRUE);
    }

    public void applyUpdateFields(Item entity, BusinessProductUpdateRequest req,
                                   BusinessBranch branch, Category category) {
        if (req.getName() != null) entity.setName(req.getName().trim());
        if (req.getDescription() != null) entity.setDescription(req.getDescription());
        if (req.getDeepLink() != null) entity.setDeepLink(req.getDeepLink());
        if (req.getTags() != null) {
            if (entity.getTags() == null) {
                entity.setTags(new java.util.ArrayList<>(req.getTags()));
            } else {
                entity.getTags().clear();
                entity.getTags().addAll(req.getTags());
            }
        }
        if (req.getAttributes() != null) entity.setAttributes(req.getAttributes());
        if (req.getPrice() != null) entity.setPrice(req.getPrice());
        if (req.getIsActive() != null) entity.setIsActive(req.getIsActive());
        if (branch != null) entity.setBranch(branch);
        if (category != null) entity.setCategory(category);
    }

    public ProductModerationItemResponse toProductModerationItemResponse(Item entity) {
        return ProductModerationItemResponse.builder()
                .productId(entity.getId())
                .productName(entity.getName())
                .businessId(entity.getBusiness().getId())
                .businessName(entity.getBusiness().getName())
                .createdAt(entity.getCreatedAt())
                .moderationNote(entity.getModerationNote())
                .moderationStatus(entity.getModerationStatus().name())
                .build();
    }
}

package kz.ask.autodump.domain;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import kz.ask.autodump.domain.dto.DraftItemDto;
import kz.ask.autodump.domain.entity.AutodumpDraftItem;
import kz.ask.autodump.domain.enums.DraftItemStatus;
import kz.ask.autodump.infrastructure.mapper.AutodumpMapper;
import kz.ask.autodump.infrastructure.repository.AutodumpAiJobRepository;
import kz.ask.autodump.infrastructure.repository.AutodumpDraftItemRepository;
import kz.ask.autodump.infrastructure.repository.AutodumpImportSessionRepository;
import kz.ask.catalog.infrastructure.repository.ProductOfferRepository;
import kz.ask.service.infrastructure.repository.ServiceBranchOfferRepository;
import kz.ask.shared.error.ErrorCode;
import kz.ask.shared.error.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AutodumpDraftServiceImpl implements AutodumpDraftService {

    private final AutodumpDraftItemRepository draftItemRepository;
    private final AutodumpImportSessionRepository sessionRepository;
    private final AutodumpAiJobRepository aiJobRepository;
    private final ProductOfferRepository productOfferRepository;
    private final ServiceBranchOfferRepository serviceBranchOfferRepository;
    private final AutodumpMapper mapper;

    @Override
    @Transactional
    public DraftItemDto createDraft(UUID sessionId, UUID aiJobId, String itemType, String title,
                                     String normalizedTitle, String categoryLabel, String subcategoryLabel,
                                     String description, BigDecimal price, String priceText, String currency,
                                     String brand, String tagsJson, String customAttributesJson,
                                     String sourceReference, String confidenceNotes, Boolean needsReview,
                                     String duplicateGroupKey) {
        var session = sessionRepository.getReferenceById(sessionId);
        var aiJob = aiJobRepository.getReferenceById(aiJobId);
        var entity = mapper.toDraftItemEntity(session, aiJob, itemType, DraftItemStatus.NEEDS_REVIEW,
                title, normalizedTitle, categoryLabel, subcategoryLabel, description, price, priceText,
                currency, brand, tagsJson, customAttributesJson, sourceReference, confidenceNotes,
                needsReview, duplicateGroupKey);
        var saved = draftItemRepository.save(entity);
        return mapper.toDraftItemDto(saved);
    }

    @Override
    public List<DraftItemDto> findBySession(UUID sessionId) {
        return draftItemRepository.findByImportSessionIdOrderByCreatedAt(sessionId).stream()
                .map(mapper::toDraftItemDto)
                .toList();
    }

    @Override
    public DraftItemDto findById(UUID draftId) {
        var entity = draftItemRepository.findById(draftId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.AUTODUMP_DRAFT_NOT_FOUND));
        return mapper.toDraftItemDto(entity);
    }

    @Override
    @Transactional
    public void updateDraft(UUID draftId, String title, String itemType, String categoryLabel,
                             String description, BigDecimal price, String priceText,
                             String brand, String tagsJson, String customAttributesJson) {
        AutodumpDraftItem entity = draftItemRepository.getReferenceById(draftId);
        if (title != null) entity.setTitle(title);
        if (itemType != null) entity.setItemType(itemType);
        if (categoryLabel != null) entity.setCategoryLabel(categoryLabel);
        if (description != null) entity.setDescription(description);
        if (price != null) entity.setPrice(price);
        if (priceText != null) entity.setPriceText(priceText);
        if (brand != null) entity.setBrand(brand);
        if (tagsJson != null) entity.setTagsJson(tagsJson);
        if (customAttributesJson != null) entity.setCustomAttributesJson(customAttributesJson);
        draftItemRepository.save(entity);
    }

    @Override
    @Transactional
    public void updateStatus(UUID draftId, DraftItemStatus status) {
        AutodumpDraftItem entity = draftItemRepository.getReferenceById(draftId);
        entity.setStatus(status);
        draftItemRepository.save(entity);
    }

    @Override
    @Transactional
    public void markPublished(UUID draftId, UUID publishedProductOfferId, UUID publishedServiceBranchOfferId) {
        AutodumpDraftItem entity = draftItemRepository.getReferenceById(draftId);
        entity.setStatus(DraftItemStatus.PUBLISHED);
        if (publishedProductOfferId != null) {
            entity.setPublishedProductOffer(productOfferRepository.getReferenceById(publishedProductOfferId));
        }
        if (publishedServiceBranchOfferId != null) {
            entity.setPublishedServiceBranchOffer(serviceBranchOfferRepository.getReferenceById(publishedServiceBranchOfferId));
        }
        draftItemRepository.save(entity);
    }

    @Override
    public List<DraftItemDto> findApprovedBySession(UUID sessionId) {
        return draftItemRepository.findByImportSessionIdAndStatus(sessionId, DraftItemStatus.APPROVED).stream()
                .map(mapper::toDraftItemDto)
                .toList();
    }
}

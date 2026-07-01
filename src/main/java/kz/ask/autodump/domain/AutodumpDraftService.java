package kz.ask.autodump.domain;

import java.util.List;
import java.util.UUID;
import kz.ask.autodump.domain.dto.DraftItemDto;
import kz.ask.autodump.domain.enums.DraftItemStatus;

public interface AutodumpDraftService {

    DraftItemDto createDraft(UUID sessionId, UUID aiJobId, String itemType, String title,
                             String normalizedTitle, String categoryLabel, String subcategoryLabel,
                             String description, java.math.BigDecimal price, String priceText, String currency,
                             String brand, String tagsJson, String customAttributesJson,
                             String sourceReference, String confidenceNotes, Boolean needsReview,
                             String duplicateGroupKey);

    List<DraftItemDto> findBySession(UUID sessionId);

    DraftItemDto findById(UUID draftId);

    void updateDraft(UUID draftId, String title, String itemType, String categoryLabel,
                     String description, java.math.BigDecimal price, String priceText,
                     String brand, String tagsJson, String customAttributesJson);

    void updateStatus(UUID draftId, DraftItemStatus status);

    void markPublished(UUID draftId, UUID publishedProductOfferId, UUID publishedServiceBranchOfferId);

    List<DraftItemDto> findApprovedBySession(UUID sessionId);
}

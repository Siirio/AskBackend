package kz.ask.autodump.infrastructure.mapper;

import java.math.BigDecimal;
import java.util.UUID;
import kz.ask.autodump.domain.dto.AiJobDto;
import kz.ask.autodump.domain.dto.AuditEventDto;
import kz.ask.autodump.domain.dto.DraftAttributeDto;
import kz.ask.autodump.domain.dto.DraftItemDto;
import kz.ask.autodump.domain.dto.ImportErrorDto;
import kz.ask.autodump.domain.dto.ImportSessionDto;
import kz.ask.autodump.domain.dto.RawInputDto;
import kz.ask.autodump.domain.entity.AutodumpAiJob;
import kz.ask.autodump.domain.entity.AutodumpAuditEvent;
import kz.ask.autodump.domain.entity.AutodumpDraftAttribute;
import kz.ask.autodump.domain.entity.AutodumpDraftItem;
import kz.ask.autodump.domain.entity.AutodumpImportError;
import kz.ask.autodump.domain.entity.AutodumpImportSession;
import kz.ask.autodump.domain.entity.AutodumpRawInput;
import kz.ask.autodump.domain.enums.AiJobStatus;
import kz.ask.autodump.domain.enums.AuditEventType;
import kz.ask.autodump.domain.enums.DraftAttributeSource;
import kz.ask.autodump.domain.enums.DraftItemStatus;
import kz.ask.autodump.domain.enums.ErrorSeverity;
import kz.ask.autodump.domain.enums.ImportSessionStatus;
import kz.ask.autodump.domain.enums.SourceType;
import kz.ask.autodump.domain.enums.StorageKind;
import kz.ask.business.domain.entity.Business;
import kz.ask.business.domain.entity.BusinessBranch;
import kz.ask.identity.domain.entity.AppUser;
import org.springframework.stereotype.Component;

@Component
public class AutodumpMapper {

    public ImportSessionDto toImportSessionDto(AutodumpImportSession entity) {
        return ImportSessionDto.builder()
                .id(entity.getId())
                .businessId(nullSafeId(entity.getBusiness()))
                .branchId(nullSafeId(entity.getBranch()))
                .createdBy(nullSafeId(entity.getCreatedBy()))
                .sourceType(entity.getSourceType() != null ? entity.getSourceType().name() : null)
                .status(entity.getStatus() != null ? entity.getStatus().name() : null)
                .inputSummary(entity.getInputSummary())
                .totalDraftCount(entity.getTotalDraftCount())
                .approvedCount(entity.getApprovedCount())
                .rejectedCount(entity.getRejectedCount())
                .errorCount(entity.getErrorCount())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .completedAt(entity.getCompletedAt())
                .build();
    }

    public AutodumpImportSession toImportSessionEntity(Business business, BusinessBranch branch, AppUser createdBy,
                                                        SourceType sourceType, ImportSessionStatus status) {
        AutodumpImportSession entity = new AutodumpImportSession();
        entity.setBusiness(business);
        entity.setBranch(branch);
        entity.setCreatedBy(createdBy);
        entity.setSourceType(sourceType);
        entity.setStatus(status);
        entity.setTotalDraftCount(0);
        entity.setApprovedCount(0);
        entity.setRejectedCount(0);
        entity.setErrorCount(0);
        return entity;
    }

    public RawInputDto toRawInputDto(AutodumpRawInput entity) {
        return RawInputDto.builder()
                .id(entity.getId())
                .importSessionId(nullSafeId(entity.getImportSession()))
                .originalFileName(entity.getOriginalFileName())
                .contentType(entity.getContentType())
                .storageKind(entity.getStorageKind() != null ? entity.getStorageKind().name() : null)
                .storageRef(entity.getStorageRef())
                .rawText(entity.getRawText())
                .sha256(entity.getSha256())
                .sizeBytes(entity.getSizeBytes())
                .build();
    }

    public AutodumpRawInput toRawInputEntity(AutodumpImportSession session, String originalFileName,
                                              String contentType, StorageKind storageKind, String rawText,
                                              String sha256, Long sizeBytes) {
        AutodumpRawInput entity = new AutodumpRawInput();
        entity.setImportSession(session);
        entity.setOriginalFileName(originalFileName);
        entity.setContentType(contentType);
        entity.setStorageKind(storageKind);
        entity.setRawText(rawText);
        entity.setSha256(sha256);
        entity.setSizeBytes(sizeBytes);
        return entity;
    }

    public AiJobDto toAiJobDto(AutodumpAiJob entity) {
        return AiJobDto.builder()
                .id(entity.getId())
                .importSessionId(nullSafeId(entity.getImportSession()))
                .rawInputId(nullSafeId(entity.getRawInput()))
                .status(entity.getStatus() != null ? entity.getStatus().name() : null)
                .provider(entity.getProvider())
                .model(entity.getModel())
                .promptVersion(entity.getPromptVersion())
                .inputTokenEstimate(entity.getInputTokenEstimate())
                .outputTokenEstimate(entity.getOutputTokenEstimate())
                .rawResponseJson(entity.getRawResponseJson())
                .errorMessage(entity.getErrorMessage())
                .attemptCount(entity.getAttemptCount())
                .startedAt(entity.getStartedAt())
                .finishedAt(entity.getFinishedAt())
                .build();
    }

    public AutodumpAiJob toAiJobEntity(AutodumpImportSession session, AutodumpRawInput rawInput,
                                        AiJobStatus status, String provider, String model, String promptVersion) {
        AutodumpAiJob entity = new AutodumpAiJob();
        entity.setImportSession(session);
        entity.setRawInput(rawInput);
        entity.setStatus(status);
        entity.setProvider(provider);
        entity.setModel(model);
        entity.setPromptVersion(promptVersion);
        entity.setAttemptCount(0);
        return entity;
    }

    public DraftItemDto toDraftItemDto(AutodumpDraftItem entity) {
        return DraftItemDto.builder()
                .id(entity.getId())
                .importSessionId(nullSafeId(entity.getImportSession()))
                .aiJobId(nullSafeId(entity.getAiJob()))
                .itemType(entity.getItemType())
                .status(entity.getStatus() != null ? entity.getStatus().name() : null)
                .title(entity.getTitle())
                .normalizedTitle(entity.getNormalizedTitle())
                .categoryLabel(entity.getCategoryLabel())
                .subcategoryLabel(entity.getSubcategoryLabel())
                .description(entity.getDescription())
                .price(entity.getPrice())
                .priceText(entity.getPriceText())
                .currency(entity.getCurrency())
                .brand(entity.getBrand())
                .tagsJson(entity.getTagsJson())
                .customAttributesJson(entity.getCustomAttributesJson())
                .sourceReference(entity.getSourceReference())
                .confidenceNotes(entity.getConfidenceNotes())
                .needsReview(entity.getNeedsReview())
                .duplicateGroupKey(entity.getDuplicateGroupKey())
                .publishedProductOfferId(nullSafeId(entity.getPublishedProductOffer()))
                .publishedServiceBranchOfferId(nullSafeId(entity.getPublishedServiceBranchOffer()))
                .build();
    }

    public AutodumpDraftItem toDraftItemEntity(AutodumpImportSession session, AutodumpAiJob aiJob,
                                                String itemType, DraftItemStatus status, String title,
                                                String normalizedTitle, String categoryLabel, String subcategoryLabel,
                                                String description, BigDecimal price, String priceText, String currency,
                                                String brand, String tagsJson, String customAttributesJson,
                                                String sourceReference, String confidenceNotes, Boolean needsReview,
                                                String duplicateGroupKey) {
        AutodumpDraftItem entity = new AutodumpDraftItem();
        entity.setImportSession(session);
        entity.setAiJob(aiJob);
        entity.setItemType(itemType);
        entity.setStatus(status);
        entity.setTitle(title);
        entity.setNormalizedTitle(normalizedTitle);
        entity.setCategoryLabel(categoryLabel);
        entity.setSubcategoryLabel(subcategoryLabel);
        entity.setDescription(description);
        entity.setPrice(price);
        entity.setPriceText(priceText);
        entity.setCurrency(currency);
        entity.setBrand(brand);
        entity.setTagsJson(tagsJson);
        entity.setCustomAttributesJson(customAttributesJson);
        entity.setSourceReference(sourceReference);
        entity.setConfidenceNotes(confidenceNotes);
        entity.setNeedsReview(needsReview);
        entity.setDuplicateGroupKey(duplicateGroupKey);
        return entity;
    }

    public DraftAttributeDto toDraftAttributeDto(AutodumpDraftAttribute entity) {
        return DraftAttributeDto.builder()
                .id(entity.getId())
                .draftItemId(nullSafeId(entity.getDraftItem()))
                .attributeKey(entity.getAttributeKey())
                .attributeValue(entity.getAttributeValue())
                .source(entity.getSource() != null ? entity.getSource().name() : null)
                .build();
    }

    public AutodumpDraftAttribute toDraftAttributeEntity(AutodumpDraftItem draftItem, String key,
                                                          String value, DraftAttributeSource source) {
        AutodumpDraftAttribute entity = new AutodumpDraftAttribute();
        entity.setDraftItem(draftItem);
        entity.setAttributeKey(key);
        entity.setAttributeValue(value);
        entity.setSource(source);
        return entity;
    }

    public AuditEventDto toAuditEventDto(AutodumpAuditEvent entity) {
        return AuditEventDto.builder()
                .id(entity.getId())
                .importSessionId(nullSafeId(entity.getImportSession()))
                .draftItemId(nullSafeId(entity.getDraftItem()))
                .actorUserId(nullSafeId(entity.getActorUser()))
                .eventType(entity.getEventType() != null ? entity.getEventType().name() : null)
                .payloadJson(entity.getPayloadJson())
                .build();
    }

    public AutodumpAuditEvent toAuditEventEntity(AutodumpImportSession session, AutodumpDraftItem draftItem,
                                                  AppUser actor, AuditEventType eventType, String payloadJson) {
        AutodumpAuditEvent entity = new AutodumpAuditEvent();
        entity.setImportSession(session);
        entity.setDraftItem(draftItem);
        entity.setActorUser(actor);
        entity.setEventType(eventType);
        entity.setPayloadJson(payloadJson);
        return entity;
    }

    public ImportErrorDto toImportErrorDto(AutodumpImportError entity) {
        return ImportErrorDto.builder()
                .id(entity.getId())
                .importSessionId(nullSafeId(entity.getImportSession()))
                .draftItemId(nullSafeId(entity.getDraftItem()))
                .severity(entity.getSeverity() != null ? entity.getSeverity().name() : null)
                .code(entity.getCode())
                .message(entity.getMessage())
                .payloadJson(entity.getPayloadJson())
                .build();
    }

    public AutodumpImportError toImportErrorEntity(AutodumpImportSession session, AutodumpDraftItem draftItem,
                                                    ErrorSeverity severity, String code, String message,
                                                    String payloadJson) {
        AutodumpImportError entity = new AutodumpImportError();
        entity.setImportSession(session);
        entity.setDraftItem(draftItem);
        entity.setSeverity(severity);
        entity.setCode(code);
        entity.setMessage(message);
        entity.setPayloadJson(payloadJson);
        return entity;
    }

    private UUID nullSafeId(Object entity) {
        if (entity == null) {
            return null;
        }
        if (entity instanceof kz.ask.shared.domain.entity.BaseUuidV7Entity base) {
            return base.getId();
        }
        return null;
    }
}

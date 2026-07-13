package kz.ask.autodump.application;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import kz.ask.autodump.api.dto.AutodumpSessionStatusResponse;
import kz.ask.autodump.api.dto.CreateAutodumpSessionRequest;
import kz.ask.autodump.api.dto.CreateAutodumpSessionResponse;
import kz.ask.autodump.api.dto.PublishResponse;
import kz.ask.autodump.api.dto.UpdateDraftRequest;
import kz.ask.autodump.domain.AutodumpAuditService;
import kz.ask.autodump.domain.AutodumpDraftService;
import kz.ask.autodump.domain.AutodumpExtractionClient;
import kz.ask.autodump.domain.AutodumpImportService;
import kz.ask.autodump.domain.dto.DraftItemDto;
import kz.ask.autodump.domain.dto.ImportSessionDto;
import kz.ask.autodump.domain.dto.RawInputDto;
import kz.ask.autodump.domain.enums.AiJobStatus;
import kz.ask.autodump.domain.enums.AuditEventType;
import kz.ask.autodump.domain.enums.DraftItemStatus;
import kz.ask.autodump.domain.enums.ImportSessionStatus;
import kz.ask.autodump.domain.enums.SourceType;
import kz.ask.business.domain.BusinessBranchService;
import kz.ask.business.domain.CategoryService;
import kz.ask.business.domain.BusinessService;
import kz.ask.business.domain.BranchMemberService;
import kz.ask.business.domain.dto.BusinessBranchDto;
import kz.ask.catalog.domain.dto.CreateProductDto;
import kz.ask.catalog.domain.dto.CreateProductOfferDto;
import kz.ask.catalog.domain.dto.ProductDto;
import kz.ask.catalog.domain.dto.ProductOfferDto;
import kz.ask.catalog.domain.service.ProductOfferService;
import kz.ask.catalog.domain.service.ProductService;
import kz.ask.identity.infrastructure.security.AskPrincipal;
import kz.ask.search.domain.SearchDocumentService;
import kz.ask.search.domain.SearchService;
import kz.ask.service.api.dto.BusinessServiceCreateRequest;
import kz.ask.service.application.ServiceBranchOfferDto;
import kz.ask.service.domain.ServiceService;
import kz.ask.shared.error.ErrorCode;
import kz.ask.shared.error.ExternalServiceException;
import kz.ask.shared.error.ForbiddenException;
import kz.ask.shared.error.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Component
@RequiredArgsConstructor
public class AutodumpImportProcessor {

    private final AutodumpImportService importService;
    private final AutodumpDraftService draftService;
    private final AutodumpAuditService auditService;
    private final AutodumpExtractionClient extractionClient;
    private final BusinessService businessService;
    private final BusinessBranchService businessBranchService;
    private final BranchMemberService branchMemberService;
    private final CategoryService categoryService;
    private final ProductService productService;
    private final ProductOfferService productOfferService;
    private final ServiceService serviceService;
    private final SearchService searchService;
    private final SearchDocumentService searchDocumentService;
    private final ObjectMapper objectMapper;

    @Transactional
    public CreateAutodumpSessionResponse createSession(AskPrincipal principal, UUID branchId,
                                                        CreateAutodumpSessionRequest request) {
        verifyBranchAccess(principal.getUserId(), branchId);
        BusinessBranchDto branch = requireBranch(branchId);

        SourceType sourceType = parseSourceType(request.getSourceType());

        ImportSessionDto session = importService.createSession(branch.getBusinessId(), branchId,
                principal.getUserId(), sourceType, request.getInputSummary());
        auditService.recordEvent(session.getId(), null, principal.getUserId(),
                AuditEventType.SESSION_CREATED, null);

        importService.updateStatus(session.getId(), ImportSessionStatus.INPUT_STORED);
        RawInputDto rawInput = importService.storeRawInput(session.getId(), null, "text/plain",
                request.getRawText());
        auditService.recordEvent(session.getId(), null, principal.getUserId(),
                AuditEventType.INPUT_STORED, null);

        var aiJob = importService.createAiJob(session.getId(), rawInput.getId(),
                "deepseek", null, "v1");

        int draftsCreated = 0;
        try {
            importService.updateAiJobStatus(aiJob.getId(), AiJobStatus.RUNNING, null);
            auditService.recordEvent(session.getId(), null, principal.getUserId(),
                    AuditEventType.AI_STARTED, null);

            JsonNode result = extractionClient.extract(request.getRawText());
            importService.completeAiJob(aiJob.getId(), result.toString(), null, null);
            auditService.recordEvent(session.getId(), null, principal.getUserId(),
                    AuditEventType.AI_SUCCEEDED, null);

            JsonNode items = result.path("items");
            if (items.isArray()) {
                for (JsonNode item : items) {
                    DraftItemDto draft = draftService.createDraft(session.getId(), aiJob.getId(),
                            item.path("item_type").asText("PRODUCT"),
                            item.path("title").asText(),
                            item.path("normalized_title").asText(),
                            item.path("category_label").asText(),
                            item.path("subcategory_label").asText(),
                            item.path("description").asText(),
                            item.has("price") && !item.get("price").isNull()
                                    ? BigDecimal.valueOf(item.get("price").asDouble()) : null,
                            item.path("price_text").asText(),
                            item.path("currency").asText(),
                            item.path("brand").asText(),
                            item.has("tags") ? item.get("tags").toString() : null,
                            item.has("custom_attributes") ? item.get("custom_attributes").toString() : null,
                            item.path("source_reference").asText(),
                            item.path("confidence_notes").asText(),
                            item.path("needs_review").asBoolean(false),
                            item.path("duplicate_group_key").asText());
                    auditService.recordEvent(session.getId(), draft.getId(), principal.getUserId(),
                            AuditEventType.DRAFT_CREATED, null);
                    draftsCreated++;
                }
            }

            importService.updateStatus(session.getId(), ImportSessionStatus.DRAFTS_READY);
            importService.updateCounts(session.getId(), draftsCreated, 0, 0, 0);
        } catch (ExternalServiceException e) {
            importService.updateAiJobStatus(aiJob.getId(), AiJobStatus.FAILED, e.getMessage());
            auditService.recordEvent(session.getId(), null, principal.getUserId(),
                    AuditEventType.AI_FAILED, e.getMessage());
            importService.updateStatus(session.getId(), ImportSessionStatus.FAILED);
            throw e;
        }

        return CreateAutodumpSessionResponse.builder()
                .sessionId(session.getId())
                .status(ImportSessionStatus.DRAFTS_READY.name())
                .rawInputId(rawInput.getId())
                .aiJobId(aiJob.getId())
                .draftsCreated(draftsCreated)
                .build();
    }

    @Transactional
    public CreateAutodumpSessionResponse createSessionFromFile(AskPrincipal principal, UUID branchId,
                                                               MultipartFile file) {
        String filename = file.getOriginalFilename() == null ? "" : file.getOriginalFilename().toLowerCase();
        String rawText = extractRawText(file, filename);
        return createSession(principal, branchId, CreateAutodumpSessionRequest.builder()
                .sourceType(sourceTypeForFilename(filename))
                .inputSummary(file.getOriginalFilename())
                .rawText(rawText)
                .build());
    }

    public AutodumpSessionStatusResponse getSessionStatus(AskPrincipal principal, UUID branchId, UUID sessionId) {
        verifyBranchAccess(principal.getUserId(), branchId);
        ImportSessionDto session = importService.findByIdAndBranch(sessionId, branchId);
        List<DraftItemDto> drafts = draftService.findBySession(sessionId);
        return buildStatusResponse(session, drafts);
    }

    public List<AutodumpSessionStatusResponse> listSessions(AskPrincipal principal, UUID branchId) {
        verifyBranchAccess(principal.getUserId(), branchId);
        List<ImportSessionDto> sessions = importService.findByBranch(branchId);
        return sessions.stream().map(s -> {
            List<DraftItemDto> drafts = draftService.findBySession(s.getId());
            return buildStatusResponse(s, drafts);
        }).toList();
    }

    public DraftItemDto getDraft(AskPrincipal principal, UUID branchId, UUID sessionId, UUID draftId) {
        verifyBranchAccess(principal.getUserId(), branchId);
        importService.findByIdAndBranch(sessionId, branchId);
        return draftService.findById(draftId);
    }

    @Transactional
    public DraftItemDto updateDraft(AskPrincipal principal, UUID branchId, UUID sessionId,
                                     UUID draftId, UpdateDraftRequest request) {
        verifyBranchAccess(principal.getUserId(), branchId);
        importService.findByIdAndBranch(sessionId, branchId);
        draftService.updateDraft(draftId, request.getTitle(), request.getItemType(),
                request.getCategoryLabel(), request.getDescription(), request.getPrice(),
                request.getPriceText(), request.getBrand(), request.getTagsJson(),
                request.getCustomAttributesJson());
        auditService.recordEvent(sessionId, draftId, principal.getUserId(),
                AuditEventType.DRAFT_EDITED, null);
        return draftService.findById(draftId);
    }

    @Transactional
    public void approveDraft(AskPrincipal principal, UUID branchId, UUID sessionId, UUID draftId) {
        verifyBranchAccess(principal.getUserId(), branchId);
        importService.findByIdAndBranch(sessionId, branchId);
        draftService.updateStatus(draftId, DraftItemStatus.APPROVED);
        auditService.recordEvent(sessionId, draftId, principal.getUserId(),
                AuditEventType.DRAFT_APPROVED, null);
    }

    @Transactional
    public void rejectDraft(AskPrincipal principal, UUID branchId, UUID sessionId, UUID draftId) {
        verifyBranchAccess(principal.getUserId(), branchId);
        importService.findByIdAndBranch(sessionId, branchId);
        draftService.updateStatus(draftId, DraftItemStatus.REJECTED);
        auditService.recordEvent(sessionId, draftId, principal.getUserId(),
                AuditEventType.DRAFT_REJECTED, null);
    }

    @Transactional
    public PublishResponse publishApproved(AskPrincipal principal, UUID branchId, UUID sessionId) {
        verifyBranchAccess(principal.getUserId(), branchId);
        ImportSessionDto session = importService.findByIdAndBranch(sessionId, branchId);
        List<DraftItemDto> approved = draftService.findApprovedBySession(sessionId);

        int published = 0;
        int skipped = 0;
        for (DraftItemDto draft : approved) {
            publishDraft(session, branchId, draft);
            auditService.recordEvent(sessionId, draft.getId(), principal.getUserId(),
                    AuditEventType.DRAFTS_PUBLISHED, null);
            published++;
        }

        importService.updateCounts(sessionId, session.getTotalDraftCount(),
                session.getApprovedCount(), session.getRejectedCount(), skipped);
        importService.updateStatus(sessionId, ImportSessionStatus.PUBLISHED);
        importService.deleteSession(sessionId);

        return PublishResponse.builder()
                .sessionId(sessionId)
                .sessionStatus(ImportSessionStatus.PUBLISHED.name())
                .published(published)
                .skipped(skipped)
                .build();
    }

    private void publishDraft(ImportSessionDto session, UUID branchId, DraftItemDto draft) {
        if (isServiceDraft(draft)) {
            ServiceBranchOfferDto offer = publishServiceDraft(session, branchId, draft);
            draftService.markPublished(draft.getId(), null, offer.getServiceBranchOfferId());
            return;
        }
        ProductOfferDto offer = publishProductDraft(session, branchId, draft);
        draftService.markPublished(draft.getId(), offer.getId(), null);
    }

    private ProductOfferDto publishProductDraft(ImportSessionDto session, UUID branchId, DraftItemDto draft) {
        ProductDto product = productService.create(CreateProductDto.builder()
                .businessId(session.getBusinessId())
                .name(resolveTitle(draft))
                .categoryLabel(resolveCategoryLabel(draft))
                .description(draft.getDescription())
                .tags(resolveTags(draft))
                .characteristics(resolveCharacteristics(draft))
                .build());
        ProductOfferDto offer = productOfferService.create(CreateProductOfferDto.builder()
                .productId(product.getId())
                .branchId(branchId)
                .price(draft.getPrice())
                .build());
        searchService.indexProductOffer(offer.getId(), product.getId(), session.getBusinessId(), branchId);
        return offer;
    }

    private ServiceBranchOfferDto publishServiceDraft(ImportSessionDto session, UUID branchId, DraftItemDto draft) {
        UUID categoryId = categoryService.resolveServiceImportCategoryId(resolveCategoryLabel(draft));
        ServiceBranchOfferDto offer = serviceService.createService(session.getBusinessId(), branchId,
                BusinessServiceCreateRequest.builder()
                        .categoryId(categoryId)
                        .name(resolveTitle(draft))
                        .description(draft.getDescription())
                        .basePrice(draft.getPrice())
                        .scheduleText(resolveScheduleText(draft))
                        .active(Boolean.TRUE)
                        .build());
        searchDocumentService.syncServiceDocument(offer.getServiceBranchOfferId(), offer.getBusinessId(),
                offer.getBranchId(), offer.getName(), serviceSearchSummary(offer), offer.getCategoryLabel(),
                offer.getBasePrice(), offer.getActive());
        return offer;
    }

    private Boolean isServiceDraft(DraftItemDto draft) {
        String itemType = draft.getItemType() == null ? "" : draft.getItemType().trim().toUpperCase();
        return itemType.contains("SERVICE");
    }

    private String resolveTitle(DraftItemDto draft) {
        if (draft.getNormalizedTitle() != null && !draft.getNormalizedTitle().isBlank()) {
            return draft.getNormalizedTitle();
        }
        return draft.getTitle();
    }

    private String resolveCategoryLabel(DraftItemDto draft) {
        if (draft.getSubcategoryLabel() != null && !draft.getSubcategoryLabel().isBlank()) {
            return draft.getSubcategoryLabel();
        }
        return draft.getCategoryLabel();
    }

    private List<String> resolveTags(DraftItemDto draft) {
        List<String> tags = parseStringList(draft.getTagsJson());
        if (draft.getBrand() != null && !draft.getBrand().isBlank()) {
            tags.add(draft.getBrand());
        }
        String categoryLabel = resolveCategoryLabel(draft);
        if (categoryLabel != null && !categoryLabel.isBlank()) {
            tags.add(categoryLabel);
        }
        return tags.stream().map(String::trim).filter(value -> !value.isBlank()).distinct().toList();
    }

    private Map<String, String> resolveCharacteristics(DraftItemDto draft) {
        Map<String, String> characteristics = parseStringMap(draft.getCustomAttributesJson());
        if (draft.getBrand() != null && !draft.getBrand().isBlank()) {
            characteristics.put("brand", draft.getBrand());
        }
        if (draft.getSourceReference() != null && !draft.getSourceReference().isBlank()) {
            characteristics.put("source_reference", draft.getSourceReference());
        }
        return characteristics;
    }

    private String resolveScheduleText(DraftItemDto draft) {
        Map<String, String> attributes = parseStringMap(draft.getCustomAttributesJson());
        for (String key : List.of("duration", "duration_text", "schedule", "time", "длительность", "время")) {
            String value = attributes.get(key);
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }

    private String serviceSearchSummary(ServiceBranchOfferDto offer) {
        return String.join(" ",
                offer.getDescription() == null ? "" : offer.getDescription(),
                offer.getScheduleText() == null ? "" : offer.getScheduleText()).trim();
    }

    private String draftSourceText(DraftItemDto draft) {
        return String.join(" ",
                draft.getTitle() == null ? "" : draft.getTitle(),
                draft.getNormalizedTitle() == null ? "" : draft.getNormalizedTitle(),
                draft.getDescription() == null ? "" : draft.getDescription(),
                draft.getPriceText() == null ? "" : draft.getPriceText(),
                draft.getSourceReference() == null ? "" : draft.getSourceReference(),
                draft.getCustomAttributesJson() == null ? "" : draft.getCustomAttributesJson());
    }

    private List<String> parseStringList(String json) {
        if (json == null || json.isBlank()) {
            return new ArrayList<>();
        }
        try {
            JsonNode node = objectMapper.readTree(json);
            List<String> values = new ArrayList<>();
            if (node.isArray()) {
                node.forEach(item -> values.add(item.asText()));
            } else if (node.isTextual()) {
                values.add(node.asText());
            }
            return values;
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    private Map<String, String> parseStringMap(String json) {
        if (json == null || json.isBlank()) {
            return new LinkedHashMap<>();
        }
        try {
            var mapType = objectMapper.getTypeFactory().constructMapType(LinkedHashMap.class, String.class, String.class);
            return objectMapper.readValue(json, mapType);
        } catch (Exception e) {
            return new LinkedHashMap<>();
        }
    }

    private BusinessBranchDto requireBranch(UUID branchId) {
        BusinessBranchDto branch = businessBranchService.findById(branchId);
        if (branch == null) {
            throw new NotFoundException(ErrorCode.BRANCH_NOT_FOUND);
        }
        return branch;
    }

    private void verifyBranchAccess(UUID userId, UUID branchId) {
        BusinessBranchDto branch = requireBranch(branchId);
        if (businessService.isOwnerOfBusiness(branch.getBusinessId(), userId)) {
            return;
        }
        if (branchMemberService.isStaffOfBranch(branchId, userId)) {
            return;
        }
        throw new ForbiddenException(ErrorCode.ACCESS_DENIED);
    }

    private SourceType parseSourceType(String sourceType) {
        try {
            return SourceType.valueOf(sourceType.toUpperCase());
        } catch (IllegalArgumentException e) {
            return SourceType.OTHER;
        }
    }

    private String extractRawText(MultipartFile file, String filename) {
        try {
            if (filename.endsWith(".txt") || filename.endsWith(".md")) {
                return new String(file.getBytes(), StandardCharsets.UTF_8);
            }
            if (filename.endsWith(".pdf")) {
                try (PDDocument document = Loader.loadPDF(file.getBytes())) {
                    return new PDFTextStripper().getText(document);
                }
            }
        } catch (IOException e) {
            throw new ExternalServiceException(ErrorCode.AUTODUMP_INPUT_READ_FAILED);
        }
        throw new ExternalServiceException(ErrorCode.AUTODUMP_INPUT_READ_FAILED);
    }

    private String sourceTypeForFilename(String filename) {
        if (filename.endsWith(".txt") || filename.endsWith(".md")) {
            return SourceType.PASTE_TEXT.name();
        }
        if (filename.endsWith(".pdf")) {
            return SourceType.PRICE_LIST.name();
        }
        return SourceType.OTHER.name();
    }

    private AutodumpSessionStatusResponse buildStatusResponse(ImportSessionDto session, List<DraftItemDto> drafts) {
        return AutodumpSessionStatusResponse.builder()
                .sessionId(session.getId())
                .businessId(session.getBusinessId())
                .branchId(session.getBranchId())
                .createdBy(session.getCreatedBy())
                .sourceType(session.getSourceType())
                .status(session.getStatus())
                .inputSummary(session.getInputSummary())
                .totalDraftCount(session.getTotalDraftCount())
                .approvedCount(session.getApprovedCount())
                .rejectedCount(session.getRejectedCount())
                .errorCount(session.getErrorCount())
                .createdAt(session.getCreatedAt())
                .updatedAt(session.getUpdatedAt())
                .completedAt(session.getCompletedAt())
                .drafts(drafts)
                .build();
    }
}

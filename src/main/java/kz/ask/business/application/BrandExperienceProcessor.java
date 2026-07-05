package kz.ask.business.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import kz.ask.business.api.dto.BrandDropRequest;
import kz.ask.business.api.dto.BrandDropResponse;
import kz.ask.business.api.dto.BrandPageBlockRequest;
import kz.ask.business.api.dto.BrandPageBlockResponse;
import kz.ask.business.api.dto.BrandProfileRequest;
import kz.ask.business.api.dto.BrandProfileResponse;
import kz.ask.business.api.dto.StorefrontBlockRequest;
import kz.ask.business.api.dto.StorefrontBlockResponse;
import kz.ask.business.api.dto.StorefrontDraftRequest;
import kz.ask.business.api.dto.StorefrontPageResponse;
import kz.ask.business.domain.BrandDropService;
import kz.ask.business.domain.BrandPageBlockService;
import kz.ask.business.domain.BrandProfileService;
import kz.ask.business.domain.BusinessService;
import kz.ask.business.domain.dto.BrandDropDto;
import kz.ask.business.domain.dto.BrandPageBlockDto;
import kz.ask.business.domain.dto.BrandProfileDto;
import kz.ask.identity.infrastructure.security.AskPrincipal;
import kz.ask.search.domain.SearchDocumentService;
import kz.ask.shared.error.ErrorCode;
import kz.ask.shared.error.ForbiddenException;
import kz.ask.shared.error.NotFoundException;
import kz.ask.shared.error.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BrandExperienceProcessor {

    private static final Integer DEFAULT_DISPLAY_ORDER = 0;
    private static final String DEFAULT_DROP_STATUS = "UPCOMING";
    private static final String DEFAULT_DROP_TYPE = "NEW_COLLECTION";

    private final BusinessService businessService;
    private final BrandProfileService brandProfileService;
    private final BrandPageBlockService brandPageBlockService;
    private final BrandDropService brandDropService;
    private final SearchDocumentService searchDocumentService;
    private final ObjectMapper objectMapper;

    public BrandProfileResponse getProfile(UUID businessId) {
        BrandProfileDto dto = brandProfileService.findByBusinessId(businessId);
        if (dto == null) {
            return BrandProfileResponse.builder()
                    .businessId(businessId)
                    .build();
        }
        return toProfileResponse(dto);
    }

    public BrandProfileResponse updateProfile(AskPrincipal principal, UUID businessId, BrandProfileRequest request) {
        verifyOwnerAccess(principal, businessId);
        return toProfileResponse(brandProfileService.save(
                businessId,
                request.getBrandColor(),
                request.getLogoUrl(),
                request.getCoverUrl(),
                request.getToneOfVoice(),
                request.getDescription(),
                request.getInstagramUrl(),
                request.getTelegramUrl(),
                request.getWebsiteUrl()));
    }

    public StorefrontPageResponse getStorefront(UUID businessId) {
        List<BrandPageBlockDto> blocks = brandPageBlockService.listPublic(businessId);
        if (blocks.isEmpty()) {
            throw new NotFoundException(ErrorCode.STOREFRONT_NOT_FOUND);
        }
        return toStorefrontPageResponse(businessId, blocks);
    }

    public StorefrontPageResponse getStorefrontDraft(AskPrincipal principal, UUID businessId) {
        verifyOwnerAccess(principal, businessId);
        return toStorefrontPageResponse(businessId, brandPageBlockService.listDraft(businessId));
    }

    public StorefrontPageResponse saveStorefrontDraft(AskPrincipal principal, UUID businessId,
                                                      StorefrontDraftRequest request) {
        verifyOwnerAccess(principal, businessId);
        List<StorefrontBlockRequest> requests = request == null || request.getBlocks() == null
                ? List.of()
                : request.getBlocks();
        List<BrandPageBlockDto> blocks = requests.stream()
                .sorted(Comparator.comparing(block -> block.getDisplayOrder() == null
                        ? DEFAULT_DISPLAY_ORDER
                        : block.getDisplayOrder()))
                .map(block -> BrandPageBlockDto.builder()
                        .businessId(businessId)
                        .blockType(block.getBlockType())
                        .displayOrder(block.getDisplayOrder() == null ? DEFAULT_DISPLAY_ORDER : block.getDisplayOrder())
                        .configJson(toJson(block.getConfig()))
                        .enabled(!Boolean.FALSE.equals(block.getEnabled()))
                        .build())
                .toList();
        return toStorefrontPageResponse(businessId, brandPageBlockService.replaceDraft(businessId, blocks));
    }

    public StorefrontPageResponse publishStorefront(AskPrincipal principal, UUID businessId) {
        verifyOwnerAccess(principal, businessId);
        return toStorefrontPageResponse(businessId, brandPageBlockService.publishDraft(businessId));
    }

    public List<BrandPageBlockResponse> replaceStorefront(AskPrincipal principal, UUID businessId,
                                                          List<BrandPageBlockRequest> requests) {
        verifyOwnerAccess(principal, businessId);
        List<BrandPageBlockRequest> safeRequests = requests == null ? List.of() : requests;
        List<BrandPageBlockDto> blocks = safeRequests.stream()
                .sorted(Comparator.comparing(request -> request.getDisplayOrder() == null
                        ? DEFAULT_DISPLAY_ORDER
                        : request.getDisplayOrder()))
                .map(request -> BrandPageBlockDto.builder()
                        .businessId(businessId)
                        .blockType(request.getBlockType())
                        .displayOrder(request.getDisplayOrder() == null ? DEFAULT_DISPLAY_ORDER : request.getDisplayOrder())
                        .configJson(request.getConfigJson())
                        .enabled(!Boolean.FALSE.equals(request.getEnabled()))
                        .build())
                .toList();
        return brandPageBlockService.replace(businessId, blocks).stream()
                .map(this::toBlockResponse)
                .toList();
    }

    public List<BrandDropResponse> getDrops(UUID businessId) {
        return brandDropService.listPublic(businessId).stream()
                .map(this::toDropResponse)
                .toList();
    }

    public BrandDropResponse createDrop(AskPrincipal principal, UUID businessId, BrandDropRequest request) {
        verifyOwnerAccess(principal, businessId);
        BrandDropDto dto = brandDropService.create(
                businessId,
                request.getName(),
                request.getDescription(),
                request.getStartDate(),
                request.getEndDate(),
                request.getType(),
                request.getStatus(),
                request.getCoverUrl(),
                request.getTags(),
                request.getProductIds());
        syncDrop(dto);
        return toDropResponse(dto);
    }

    public BrandDropResponse updateDrop(AskPrincipal principal, UUID businessId, UUID dropId, BrandDropRequest request) {
        verifyOwnerAccess(principal, businessId);
        BrandDropDto dto = brandDropService.update(
                businessId,
                dropId,
                request.getName(),
                request.getDescription(),
                request.getStartDate(),
                request.getEndDate(),
                request.getType() == null ? DEFAULT_DROP_TYPE : request.getType(),
                request.getStatus() == null ? DEFAULT_DROP_STATUS : request.getStatus(),
                request.getCoverUrl(),
                request.getTags(),
                request.getProductIds());
        syncDrop(dto);
        return toDropResponse(dto);
    }

    public BrandDropResponse cancelDrop(AskPrincipal principal, UUID businessId, UUID dropId) {
        verifyOwnerAccess(principal, businessId);
        BrandDropDto dto = brandDropService.cancel(businessId, dropId);
        searchDocumentService.archiveDropDocument(dropId);
        return toDropResponse(dto);
    }

    public void deleteDrop(AskPrincipal principal, UUID businessId, UUID dropId) {
        verifyOwnerAccess(principal, businessId);
        searchDocumentService.deleteDropDocument(dropId);
        brandDropService.delete(businessId, dropId);
    }

    private void verifyOwnerAccess(AskPrincipal principal, UUID businessId) {
        if (principal == null || !businessService.isOwnerOfBusiness(businessId, principal.getUserId())) {
            throw new ForbiddenException(ErrorCode.ACCESS_DENIED);
        }
    }

    private void syncDrop(BrandDropDto dto) {
        searchDocumentService.syncDropDocument(
                dto.getId(),
                dto.getBusinessId(),
                dto.getName(),
                dto.getDescription(),
                dto.getTags(),
                isLiveDrop(dto));
    }

    private Boolean isLiveDrop(BrandDropDto dto) {
        return "ACTIVE".equals(dto.getStatus()) || "UPCOMING".equals(dto.getStatus());
    }

    private StorefrontPageResponse toStorefrontPageResponse(UUID businessId, List<BrandPageBlockDto> blocks) {
        return StorefrontPageResponse.builder()
                .businessId(businessId)
                .brandProfile(getProfile(businessId))
                .blocks(blocks.stream().map(this::toStorefrontBlockResponse).toList())
                .publishedAt(blocks.stream()
                        .map(BrandPageBlockDto::getUpdatedAt)
                        .filter(updatedAt -> updatedAt != null)
                        .max(Instant::compareTo)
                        .orElse(null))
                .build();
    }

    private BrandProfileResponse toProfileResponse(BrandProfileDto dto) {
        return BrandProfileResponse.builder()
                .id(dto.getId())
                .businessId(dto.getBusinessId())
                .businessName(dto.getBusinessName())
                .brandColor(dto.getBrandColor())
                .logoUrl(dto.getLogoUrl())
                .coverUrl(dto.getCoverUrl())
                .toneOfVoice(dto.getToneOfVoice())
                .description(dto.getDescription())
                .instagramUrl(dto.getInstagramUrl())
                .telegramUrl(dto.getTelegramUrl())
                .websiteUrl(dto.getWebsiteUrl())
                .build();
    }

    private BrandPageBlockResponse toBlockResponse(BrandPageBlockDto dto) {
        return BrandPageBlockResponse.builder()
                .id(dto.getId())
                .businessId(dto.getBusinessId())
                .blockType(dto.getBlockType())
                .displayOrder(dto.getDisplayOrder())
                .configJson(dto.getConfigJson())
                .enabled(dto.getEnabled())
                .build();
    }

    private StorefrontBlockResponse toStorefrontBlockResponse(BrandPageBlockDto dto) {
        return StorefrontBlockResponse.builder()
                .blockId(dto.getId())
                .blockType(dto.getBlockType())
                .displayOrder(dto.getDisplayOrder())
                .config(toJsonNode(dto.getConfigJson()))
                .enabled(dto.getEnabled())
                .build();
    }

    private BrandDropResponse toDropResponse(BrandDropDto dto) {
        return BrandDropResponse.builder()
                .id(dto.getId())
                .businessId(dto.getBusinessId())
                .name(dto.getName())
                .description(dto.getDescription())
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .type(dto.getType())
                .status(dto.getStatus())
                .coverUrl(dto.getCoverUrl())
                .productCount(dto.getProductCount())
                .tags(dto.getTags())
                .productIds(dto.getProductIds())
                .build();
    }

    private String toJson(JsonNode config) {
        if (config == null || config.isNull()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(config);
        } catch (JsonProcessingException ex) {
            throw new ValidationException(ErrorCode.STOREFRONT_BLOCK_INVALID);
        }
    }

    private JsonNode toJsonNode(String configJson) {
        if (configJson == null || configJson.isBlank()) {
            return objectMapper.createObjectNode();
        }
        try {
            return objectMapper.readTree(configJson);
        } catch (JsonProcessingException ex) {
            throw new ValidationException(ErrorCode.STOREFRONT_BLOCK_INVALID);
        }
    }
}

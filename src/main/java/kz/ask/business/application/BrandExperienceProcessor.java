package kz.ask.business.application;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import kz.ask.business.api.dto.BrandDropRequest;
import kz.ask.business.api.dto.BrandDropResponse;
import kz.ask.business.api.dto.BrandPageBlockRequest;
import kz.ask.business.api.dto.BrandPageBlockResponse;
import kz.ask.business.api.dto.BrandProfileRequest;
import kz.ask.business.api.dto.BrandProfileResponse;
import kz.ask.business.domain.BrandDropService;
import kz.ask.business.domain.BrandPageBlockService;
import kz.ask.business.domain.BrandProfileService;
import kz.ask.business.domain.BusinessService;
import kz.ask.business.domain.dto.BrandDropDto;
import kz.ask.business.domain.dto.BrandPageBlockDto;
import kz.ask.business.domain.dto.BrandProfileDto;
import kz.ask.identity.infrastructure.security.AskPrincipal;
import kz.ask.shared.error.ErrorCode;
import kz.ask.shared.error.ForbiddenException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BrandExperienceProcessor {

    private final BusinessService businessService;
    private final BrandProfileService brandProfileService;
    private final BrandPageBlockService brandPageBlockService;
    private final BrandDropService brandDropService;

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

    public List<BrandPageBlockResponse> getStorefront(UUID businessId) {
        return brandPageBlockService.listPublic(businessId).stream()
                .map(this::toBlockResponse)
                .toList();
    }

    public List<BrandPageBlockResponse> replaceStorefront(AskPrincipal principal, UUID businessId,
                                                          List<BrandPageBlockRequest> requests) {
        verifyOwnerAccess(principal, businessId);
        List<BrandPageBlockRequest> safeRequests = requests == null ? List.of() : requests;
        List<BrandPageBlockDto> blocks = safeRequests.stream()
                .sorted(Comparator.comparing(request -> request.getDisplayOrder() == null ? 0 : request.getDisplayOrder()))
                .map(request -> BrandPageBlockDto.builder()
                        .businessId(businessId)
                        .blockType(request.getBlockType())
                        .displayOrder(request.getDisplayOrder())
                        .configJson(request.getConfigJson())
                        .enabled(request.getEnabled())
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
        String status = request.getStatus() == null ? "UPCOMING" : request.getStatus();
        String type = request.getType() == null ? "NEW_COLLECTION" : request.getType();
        return toDropResponse(brandDropService.create(
                businessId,
                request.getName(),
                request.getDescription(),
                request.getStartDate(),
                request.getEndDate(),
                type,
                status,
                request.getCoverUrl()));
    }

    private void verifyOwnerAccess(AskPrincipal principal, UUID businessId) {
        if (principal == null || !businessService.isOwnerOfBusiness(businessId, principal.getUserId())) {
            throw new ForbiddenException(ErrorCode.ACCESS_DENIED);
        }
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
                .build();
    }
}

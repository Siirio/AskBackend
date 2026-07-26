package kz.ask.business.profile.application;

import java.util.UUID;
import kz.ask.business.core.domain.BusinessService;
import kz.ask.business.profile.api.dto.BusinessProfileRequest;
import kz.ask.business.profile.api.dto.BusinessProfileResponse;
import kz.ask.business.profile.domain.BusinessProfileService;
import kz.ask.business.profile.domain.dto.BusinessProfileDto;
import kz.ask.identity.infrastructure.security.AskPrincipal;
import kz.ask.shared.error.ErrorCode;
import kz.ask.shared.error.ForbiddenException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BusinessProfileProcessor {

    private final BusinessService businessService;
    private final BusinessProfileService businessProfileService;

    public BusinessProfileResponse get(UUID businessId) {
        return toResponse(businessProfileService.findByBusinessId(businessId));
    }

    public BusinessProfileResponse update(
            AskPrincipal principal,
            UUID businessId,
            BusinessProfileRequest request) {
        if (!businessService.isManagerOrAboveOfBusiness(businessId, principal.getUserId())) {
            throw new ForbiddenException(ErrorCode.ACCESS_DENIED);
        }
        return toResponse(businessProfileService.save(
                businessId,
                request.getBrandColor(),
                null,
                null,
                request.getDescription(),
                request.getNumber(),
                request.getEmail(),
                request.getInstagramUrl(),
                request.getTelegramUrl(),
                request.getWebsiteUrl()));
    }

    private BusinessProfileResponse toResponse(BusinessProfileDto dto) {
        if (dto == null) {
            return null;
        }
        return BusinessProfileResponse.builder()
                .id(dto.getId())
                .businessId(dto.getBusinessId())
                .businessName(dto.getBusinessName())
                .brandColor(dto.getBrandColor())
                .logoUrl(dto.getLogoUrl())
                .coverUrl(dto.getCoverUrl())
                .description(dto.getDescription())
                .number(dto.getNumber())
                .email(dto.getEmail())
                .instagramUrl(dto.getInstagramUrl())
                .telegramUrl(dto.getTelegramUrl())
                .websiteUrl(dto.getWebsiteUrl())
                .build();
    }
}

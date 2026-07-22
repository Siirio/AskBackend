package kz.ask.business.profile.api;

import java.util.UUID;
import kz.ask.business.profile.api.dto.BusinessProfileRequest;
import kz.ask.business.profile.api.dto.BusinessProfileResponse;
import kz.ask.business.profile.domain.BusinessProfileService;
import kz.ask.business.profile.domain.dto.BusinessProfileDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/businesses")
@RequiredArgsConstructor
public class BusinessProfileController {

    private final BusinessProfileService businessProfileService;

    @GetMapping("/{businessId}/business-profile")
    public ResponseEntity<BusinessProfileResponse> get(@PathVariable UUID businessId) {
        BusinessProfileDto dto = businessProfileService.findByBusinessId(businessId);
        if (dto == null) {
            return ResponseEntity.ok(null);
        }
        return ResponseEntity.ok(toResponse(dto));
    }

    @PutMapping("/{businessId}/business-profile")
    public ResponseEntity<BusinessProfileResponse> update(@PathVariable UUID businessId,
                                                           @RequestBody BusinessProfileRequest request) {
        BusinessProfileDto dto = businessProfileService.save(businessId,
                request.getBrandColor(), request.getLogoUrl(), request.getCoverUrl(),
                request.getDescription(), request.getNumber(), request.getEmail(),
                request.getInstagramUrl(), request.getTelegramUrl(), request.getWebsiteUrl());
        return ResponseEntity.ok(toResponse(dto));
    }

    private BusinessProfileResponse toResponse(BusinessProfileDto dto) {
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

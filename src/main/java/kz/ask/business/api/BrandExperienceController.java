package kz.ask.business.api;

import java.util.List;
import java.util.UUID;
import kz.ask.business.api.dto.BrandDropRequest;
import kz.ask.business.api.dto.BrandDropResponse;
import kz.ask.business.api.dto.BrandPageBlockRequest;
import kz.ask.business.api.dto.BrandPageBlockResponse;
import kz.ask.business.api.dto.BrandProfileRequest;
import kz.ask.business.api.dto.BrandProfileResponse;
import kz.ask.business.application.BrandExperienceProcessor;
import kz.ask.identity.infrastructure.security.AskPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/businesses/{businessId}")
@RequiredArgsConstructor
public class BrandExperienceController {

    private final BrandExperienceProcessor brandExperienceProcessor;

    @GetMapping("/brand-profile")
    public ResponseEntity<BrandProfileResponse> getProfile(@PathVariable UUID businessId) {
        return ResponseEntity.ok(brandExperienceProcessor.getProfile(businessId));
    }

    @PutMapping("/brand-profile")
    public ResponseEntity<BrandProfileResponse> updateProfile(@AuthenticationPrincipal AskPrincipal principal,
                                                              @PathVariable UUID businessId,
                                                              @RequestBody BrandProfileRequest request) {
        return ResponseEntity.ok(brandExperienceProcessor.updateProfile(principal, businessId, request));
    }

    @GetMapping("/storefront")
    public ResponseEntity<List<BrandPageBlockResponse>> getStorefront(@PathVariable UUID businessId) {
        return ResponseEntity.ok(brandExperienceProcessor.getStorefront(businessId));
    }

    @PutMapping("/storefront")
    public ResponseEntity<List<BrandPageBlockResponse>> replaceStorefront(@AuthenticationPrincipal AskPrincipal principal,
                                                                          @PathVariable UUID businessId,
                                                                          @RequestBody List<BrandPageBlockRequest> request) {
        return ResponseEntity.ok(brandExperienceProcessor.replaceStorefront(principal, businessId, request));
    }

    @GetMapping("/drops")
    public ResponseEntity<List<BrandDropResponse>> getDrops(@PathVariable UUID businessId) {
        return ResponseEntity.ok(brandExperienceProcessor.getDrops(businessId));
    }

    @PostMapping("/drops")
    public ResponseEntity<BrandDropResponse> createDrop(@AuthenticationPrincipal AskPrincipal principal,
                                                        @PathVariable UUID businessId,
                                                        @RequestBody BrandDropRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(brandExperienceProcessor.createDrop(principal, businessId, request));
    }
}

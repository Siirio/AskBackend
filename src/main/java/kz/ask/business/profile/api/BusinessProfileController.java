package kz.ask.business.profile.api;

import jakarta.validation.Valid;
import java.util.UUID;
import kz.ask.business.profile.api.dto.BusinessProfileRequest;
import kz.ask.business.profile.api.dto.BusinessProfileResponse;
import kz.ask.business.profile.application.BusinessProfileProcessor;
import kz.ask.business.media.application.BusinessMediaProcessor;
import kz.ask.identity.infrastructure.security.AskPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/businesses")
@RequiredArgsConstructor
public class BusinessProfileController {

    private final BusinessProfileProcessor businessProfileProcessor;
    private final BusinessMediaProcessor businessMediaProcessor;

    @GetMapping("/{businessId}/business-profile")
    public ResponseEntity<BusinessProfileResponse> get(@PathVariable UUID businessId) {
        return ResponseEntity.ok(businessProfileProcessor.get(businessId));
    }

    @PatchMapping("/{businessId}/business-profile")
    public ResponseEntity<BusinessProfileResponse> update(
            @AuthenticationPrincipal AskPrincipal principal,
            @PathVariable UUID businessId,
            @Valid @RequestBody BusinessProfileRequest request) {
        return ResponseEntity.ok(businessProfileProcessor.update(principal, businessId, request));
    }

    @PostMapping("/{businessId}/business-profile/logo")
    public ResponseEntity<BusinessProfileResponse> uploadLogo(
            @AuthenticationPrincipal AskPrincipal principal,
            @PathVariable UUID businessId,
            @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(businessMediaProcessor.uploadProfileLogo(principal, businessId, file));
    }

    @PostMapping("/{businessId}/business-profile/cover")
    public ResponseEntity<BusinessProfileResponse> uploadCover(
            @AuthenticationPrincipal AskPrincipal principal,
            @PathVariable UUID businessId,
            @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(businessMediaProcessor.uploadProfileCover(principal, businessId, file));
    }
}

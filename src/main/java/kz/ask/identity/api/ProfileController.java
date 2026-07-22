package kz.ask.identity.api;

import jakarta.validation.Valid;
import kz.ask.identity.api.dto.CustomerProfileResponse;
import kz.ask.identity.api.dto.UpdateCustomerProfileRequest;
import kz.ask.identity.application.ProfileProcessor;
import kz.ask.identity.infrastructure.security.AskPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileProcessor profileProcessor;

    @GetMapping
    public ResponseEntity<CustomerProfileResponse> get(@AuthenticationPrincipal AskPrincipal principal) {
        return ResponseEntity.ok(profileProcessor.getProfile(principal));
    }

    @PatchMapping
    public ResponseEntity<CustomerProfileResponse> update(@AuthenticationPrincipal AskPrincipal principal,
                                                           @Valid @RequestBody UpdateCustomerProfileRequest req) {
        return ResponseEntity.ok(profileProcessor.updateProfile(principal, req));
    }

    @PostMapping("/icon")
    public ResponseEntity<CustomerProfileResponse> updateIcon(@AuthenticationPrincipal AskPrincipal principal,
                                                               @RequestParam String iconFileId) {
        return ResponseEntity.ok(profileProcessor.updateIcon(principal, iconFileId));
    }
}

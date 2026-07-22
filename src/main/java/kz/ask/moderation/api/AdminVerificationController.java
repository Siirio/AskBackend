package kz.ask.moderation.api;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import kz.ask.identity.infrastructure.security.AskPrincipal;
import kz.ask.moderation.api.dto.ReviewVerificationRequest;
import kz.ask.moderation.api.dto.VerificationDetailResponse;
import kz.ask.moderation.api.dto.VerificationListResponse;
import kz.ask.moderation.application.AdminVerificationProcessor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/platform/verifications")
@RequiredArgsConstructor
public class AdminVerificationController {

    private final AdminVerificationProcessor processor;

    @GetMapping
    public ResponseEntity<List<VerificationListResponse>> listPending(
            @AuthenticationPrincipal AskPrincipal principal) {
        return ResponseEntity.ok(processor.listPending(principal));
    }

    @GetMapping("/{businessId}")
    public ResponseEntity<VerificationDetailResponse> getDetail(
            @AuthenticationPrincipal AskPrincipal principal,
            @PathVariable UUID businessId) {
        return ResponseEntity.ok(processor.getDetail(principal, businessId));
    }

    @PostMapping("/{businessId}/review")
    public ResponseEntity<VerificationDetailResponse> review(
            @AuthenticationPrincipal AskPrincipal principal,
            @PathVariable UUID businessId,
            @Valid @RequestBody ReviewVerificationRequest request) {
        return ResponseEntity.ok(processor.review(principal, businessId, request));
    }
}

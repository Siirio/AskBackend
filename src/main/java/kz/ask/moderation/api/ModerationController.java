package kz.ask.moderation.api;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import kz.ask.identity.infrastructure.security.AskPrincipal;
import kz.ask.moderation.api.dto.ContentReportResponse;
import kz.ask.moderation.api.dto.CreateContentReportRequest;
import kz.ask.moderation.api.dto.ModerateBusinessRequest;
import kz.ask.moderation.api.dto.ModerateProductRequest;
import kz.ask.moderation.application.ModerationProcessor;
import kz.ask.moderation.domain.enums.ContentReportStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ModerationController {

    private final ModerationProcessor moderationProcessor;

    @PostMapping("/reports")
    public ResponseEntity<ContentReportResponse> report(
            @AuthenticationPrincipal AskPrincipal principal,
            @Valid @RequestBody CreateContentReportRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(moderationProcessor.report(principal, request));
    }

    @GetMapping("/platform/reports")
    public ResponseEntity<List<ContentReportResponse>> listOpen(
            @AuthenticationPrincipal AskPrincipal principal) {
        return ResponseEntity.ok(moderationProcessor.listOpen(principal));
    }

    @PatchMapping("/platform/reports/{reportId}")
    public ResponseEntity<ContentReportResponse> resolve(
            @AuthenticationPrincipal AskPrincipal principal,
            @PathVariable UUID reportId,
            @RequestParam ContentReportStatus status) {
        return ResponseEntity.ok(
                moderationProcessor.resolve(principal, reportId, status));
    }

    @PatchMapping("/platform/businesses/{businessId}/moderation")
    public ResponseEntity<Void> moderateBusiness(
            @AuthenticationPrincipal AskPrincipal principal,
            @PathVariable UUID businessId,
            @Valid @RequestBody ModerateBusinessRequest request) {
        moderationProcessor.moderateBusiness(
                principal, businessId, request.getStatus());
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/platform/products/{productId}/moderation")
    public ResponseEntity<Void> moderateProduct(
            @AuthenticationPrincipal AskPrincipal principal,
            @PathVariable UUID productId,
            @Valid @RequestBody ModerateProductRequest request) {
        moderationProcessor.moderateProduct(
                principal, productId, request.getHidden());
        return ResponseEntity.noContent().build();
    }
}

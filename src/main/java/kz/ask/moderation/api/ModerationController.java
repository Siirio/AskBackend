package kz.ask.moderation.api;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import kz.ask.identity.infrastructure.security.AskPrincipal;
import kz.ask.moderation.api.dto.ContentReportResponse;
import kz.ask.moderation.api.dto.ContentReportResponse;
import kz.ask.moderation.api.dto.CreateContentReportRequest;
import kz.ask.moderation.api.dto.ModerateProductRequest;
import kz.ask.moderation.api.dto.ModerationActionRequest;
import kz.ask.moderation.api.dto.ModerationActionResponse;
import kz.ask.moderation.api.dto.ProductModerationItemResponse;
import kz.ask.moderation.api.dto.RejectProductRequest;
import kz.ask.moderation.application.ModerationProcessor;
import kz.ask.platform.domain.enums.ModerationStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
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
            @RequestParam ModerationStatus status,
            @RequestBody(required = false) String note) {
        return ResponseEntity.ok(
                moderationProcessor.resolve(principal, reportId, status, note));
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

    @GetMapping("/platform/moderation/queue")
    public ResponseEntity<Page<ProductModerationItemResponse>> moderationQueue(
            @AuthenticationPrincipal AskPrincipal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(moderationProcessor.listModerationQueue(principal, page, size));
    }

    @PostMapping("/platform/moderation/queue/{productId}/approve")
    public ResponseEntity<Void> approveProduct(
            @AuthenticationPrincipal AskPrincipal principal,
            @PathVariable UUID productId) {
        moderationProcessor.approveProduct(principal, productId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/platform/moderation/queue/{productId}/reject")
    public ResponseEntity<Void> rejectProduct(
            @AuthenticationPrincipal AskPrincipal principal,
            @PathVariable UUID productId,
            @RequestBody RejectProductRequest request) {
        moderationProcessor.rejectProduct(principal, productId, request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/platform/moderation-actions")
    public ResponseEntity<ModerationActionResponse> executeModerationAction(
            @AuthenticationPrincipal AskPrincipal principal,
            @Valid @RequestBody ModerationActionRequest request) {
        return ResponseEntity.ok(moderationProcessor.executeModerationAction(principal, request));
    }
}

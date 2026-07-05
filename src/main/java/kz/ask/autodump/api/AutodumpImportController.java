package kz.ask.autodump.api;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import kz.ask.autodump.api.dto.AutodumpSessionStatusResponse;
import kz.ask.autodump.api.dto.CreateAutodumpSessionRequest;
import kz.ask.autodump.api.dto.CreateAutodumpSessionResponse;
import kz.ask.autodump.api.dto.PublishResponse;
import kz.ask.autodump.api.dto.UpdateDraftRequest;
import kz.ask.autodump.application.AutodumpImportProcessor;
import kz.ask.autodump.domain.dto.DraftItemDto;
import kz.ask.identity.infrastructure.security.AskPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/business-admin/branches/{branchId}/autodump-sessions")
@RequiredArgsConstructor
public class AutodumpImportController {

    private final AutodumpImportProcessor processor;

    @PostMapping
    public ResponseEntity<CreateAutodumpSessionResponse> createSession(
            @AuthenticationPrincipal AskPrincipal principal,
            @PathVariable UUID branchId,
            @Valid @RequestBody CreateAutodumpSessionRequest request) {
        CreateAutodumpSessionResponse response = processor.createSession(principal, branchId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping(value = "/files", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CreateAutodumpSessionResponse> createSessionFromFile(
            @AuthenticationPrincipal AskPrincipal principal,
            @PathVariable UUID branchId,
            @RequestParam("file") MultipartFile file) {
        CreateAutodumpSessionResponse response = processor.createSessionFromFile(principal, branchId, file);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<AutodumpSessionStatusResponse>> listSessions(
            @AuthenticationPrincipal AskPrincipal principal,
            @PathVariable UUID branchId) {
        return ResponseEntity.ok(processor.listSessions(principal, branchId));
    }

    @GetMapping("/{sessionId}")
    public ResponseEntity<AutodumpSessionStatusResponse> getSessionStatus(
            @AuthenticationPrincipal AskPrincipal principal,
            @PathVariable UUID branchId,
            @PathVariable UUID sessionId) {
        return ResponseEntity.ok(processor.getSessionStatus(principal, branchId, sessionId));
    }

    @GetMapping("/{sessionId}/drafts/{draftId}")
    public ResponseEntity<DraftItemDto> getDraft(
            @AuthenticationPrincipal AskPrincipal principal,
            @PathVariable UUID branchId,
            @PathVariable UUID sessionId,
            @PathVariable UUID draftId) {
        return ResponseEntity.ok(processor.getDraft(principal, branchId, sessionId, draftId));
    }

    @PutMapping("/{sessionId}/drafts/{draftId}")
    public ResponseEntity<DraftItemDto> updateDraft(
            @AuthenticationPrincipal AskPrincipal principal,
            @PathVariable UUID branchId,
            @PathVariable UUID sessionId,
            @PathVariable UUID draftId,
            @Valid @RequestBody UpdateDraftRequest request) {
        return ResponseEntity.ok(processor.updateDraft(principal, branchId, sessionId, draftId, request));
    }

    @PostMapping("/{sessionId}/drafts/{draftId}/approve")
    public ResponseEntity<Void> approveDraft(
            @AuthenticationPrincipal AskPrincipal principal,
            @PathVariable UUID branchId,
            @PathVariable UUID sessionId,
            @PathVariable UUID draftId) {
        processor.approveDraft(principal, branchId, sessionId, draftId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{sessionId}/drafts/{draftId}/reject")
    public ResponseEntity<Void> rejectDraft(
            @AuthenticationPrincipal AskPrincipal principal,
            @PathVariable UUID branchId,
            @PathVariable UUID sessionId,
            @PathVariable UUID draftId) {
        processor.rejectDraft(principal, branchId, sessionId, draftId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{sessionId}/publish")
    public ResponseEntity<PublishResponse> publishApproved(
            @AuthenticationPrincipal AskPrincipal principal,
            @PathVariable UUID branchId,
            @PathVariable UUID sessionId) {
        return ResponseEntity.ok(processor.publishApproved(principal, branchId, sessionId));
    }
}

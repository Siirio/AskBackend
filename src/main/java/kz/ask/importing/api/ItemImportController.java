package kz.ask.importing.api;

import jakarta.validation.Valid;
import java.util.UUID;
import kz.ask.business.category.domain.enums.CategoryType;
import kz.ask.identity.infrastructure.security.AskPrincipal;
import kz.ask.importing.api.dto.ItemImportApproveResponse;
import kz.ask.importing.api.dto.ItemImportCancelResponse;
import kz.ask.importing.api.dto.ItemImportMappingRequest;
import kz.ask.importing.api.dto.ItemImportPreviewResponse;
import kz.ask.importing.api.dto.ItemImportUploadResponse;
import kz.ask.importing.application.ItemImportProcessor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
public class ItemImportController {

    private final ItemImportProcessor processor;

    @PostMapping(path = "/api/v1/businesses/{businessId}/item-imports", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ItemImportUploadResponse> upload(
            @AuthenticationPrincipal AskPrincipal principal,
            @PathVariable UUID businessId,
            @RequestParam(required = false) UUID branchId,
            @RequestParam CategoryType type,
            @RequestParam("file") MultipartFile file) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(processor.upload(principal, businessId, branchId, type, file));
    }

    @PostMapping("/api/v1/item-imports/{importId}/mapping")
    public ResponseEntity<ItemImportPreviewResponse> map(
            @AuthenticationPrincipal AskPrincipal principal,
            @PathVariable UUID importId,
            @Valid @RequestBody ItemImportMappingRequest request) {
        return ResponseEntity.ok(processor.map(principal, importId, request));
    }

    @GetMapping("/api/v1/item-imports/{importId}/preview")
    public ResponseEntity<ItemImportPreviewResponse> preview(
            @AuthenticationPrincipal AskPrincipal principal,
            @PathVariable UUID importId) {
        return ResponseEntity.ok(processor.preview(principal, importId));
    }

    @PostMapping("/api/v1/item-imports/{importId}/approve")
    public ResponseEntity<ItemImportApproveResponse> approve(
            @AuthenticationPrincipal AskPrincipal principal,
            @PathVariable UUID importId) {
        return ResponseEntity.ok(processor.approve(principal, importId));
    }

    @PostMapping("/api/v1/item-imports/{importId}/cancel")
    public ResponseEntity<ItemImportCancelResponse> cancel(
            @AuthenticationPrincipal AskPrincipal principal,
            @PathVariable UUID importId) {
        return ResponseEntity.ok(processor.cancel(principal, importId));
    }
}

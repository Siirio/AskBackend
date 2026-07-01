package kz.ask.catalog.api;

import java.util.UUID;
import kz.ask.catalog.api.dto.ApproveResponse;
import kz.ask.catalog.api.dto.CancelResponse;
import kz.ask.catalog.api.dto.MappingRequest;
import kz.ask.catalog.api.dto.PreviewResponse;
import kz.ask.catalog.api.dto.UploadResponse;
import kz.ask.catalog.application.ProductImportProcessor;
import kz.ask.identity.infrastructure.security.AskPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/business-admin/branches/{branchId}/product-imports")
@RequiredArgsConstructor
public class CatalogImportController {

    private final ProductImportProcessor processor;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UploadResponse> upload(@AuthenticationPrincipal AskPrincipal principal,
                                                  @PathVariable UUID branchId,
                                                  @RequestParam("file") MultipartFile file) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(processor.upload(principal, branchId, file));
    }

    @PostMapping("/{importId}/mapping")
    public ResponseEntity<PreviewResponse> mapColumns(@AuthenticationPrincipal AskPrincipal principal,
                                                       @PathVariable UUID branchId,
                                                       @PathVariable UUID importId,
                                                       @Valid @RequestBody MappingRequest request) {
        return ResponseEntity.ok(processor.mapColumns(principal, branchId, importId, request));
    }

    @GetMapping("/{importId}/preview")
    public ResponseEntity<PreviewResponse> getPreview(@AuthenticationPrincipal AskPrincipal principal,
                                                       @PathVariable UUID branchId,
                                                       @PathVariable UUID importId) {
        return ResponseEntity.ok(processor.getPreview(principal, branchId, importId));
    }

    @PostMapping("/{importId}/approve")
    public ResponseEntity<ApproveResponse> approve(@AuthenticationPrincipal AskPrincipal principal,
                                                    @PathVariable UUID branchId,
                                                    @PathVariable UUID importId) {
        return ResponseEntity.ok(processor.approve(principal, branchId, importId));
    }

    @PostMapping("/{importId}/cancel")
    public ResponseEntity<CancelResponse> cancel(@AuthenticationPrincipal AskPrincipal principal,
                                                  @PathVariable UUID branchId,
                                                  @PathVariable UUID importId) {
        return ResponseEntity.ok(processor.cancel(principal, branchId, importId));
    }
}

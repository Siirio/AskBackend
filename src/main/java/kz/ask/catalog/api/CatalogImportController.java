package kz.ask.catalog.api;

import java.util.UUID;
import kz.ask.catalog.api.dto.ApproveResponse;
import kz.ask.catalog.api.dto.CancelResponse;
import kz.ask.catalog.api.dto.MappingRequest;
import kz.ask.catalog.api.dto.PreviewResponse;
import kz.ask.catalog.api.dto.UploadResponse;
import kz.ask.catalog.application.ProductImportProcessor;
import kz.ask.identity.infrastructure.security.AskPrincipal;
import kz.ask.shared.api.dto.EntityResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/business-admin/branches/{branchId}/product-imports")
@RequiredArgsConstructor
public class CatalogImportController {

    private final ProductImportProcessor processor;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public EntityResponse<UploadResponse> upload(@AuthenticationPrincipal AskPrincipal principal,
                                                  @PathVariable UUID branchId,
                                                  @RequestParam("file") MultipartFile file) {
        return EntityResponse.<UploadResponse>builder()
            .data(processor.upload(principal, branchId, file))
            .build();
    }

    @PostMapping("/{importId}/mapping")
    public EntityResponse<PreviewResponse> mapColumns(@AuthenticationPrincipal AskPrincipal principal,
                                                       @PathVariable UUID branchId,
                                                       @PathVariable UUID importId,
                                                       @Valid @RequestBody MappingRequest request) {
        return EntityResponse.<PreviewResponse>builder()
            .data(processor.mapColumns(principal, branchId, importId, request))
            .build();
    }

    @GetMapping("/{importId}/preview")
    public EntityResponse<PreviewResponse> getPreview(@AuthenticationPrincipal AskPrincipal principal,
                                                       @PathVariable UUID branchId,
                                                       @PathVariable UUID importId) {
        return EntityResponse.<PreviewResponse>builder()
            .data(processor.getPreview(principal, branchId, importId))
            .build();
    }

    @PostMapping("/{importId}/approve")
    public EntityResponse<ApproveResponse> approve(@AuthenticationPrincipal AskPrincipal principal,
                                                    @PathVariable UUID branchId,
                                                    @PathVariable UUID importId) {
        return EntityResponse.<ApproveResponse>builder()
            .data(processor.approve(principal, branchId, importId))
            .build();
    }

    @PostMapping("/{importId}/cancel")
    public EntityResponse<CancelResponse> cancel(@AuthenticationPrincipal AskPrincipal principal,
                                                  @PathVariable UUID branchId,
                                                  @PathVariable UUID importId) {
        return EntityResponse.<CancelResponse>builder()
            .data(processor.cancel(principal, branchId, importId))
            .build();
    }
}

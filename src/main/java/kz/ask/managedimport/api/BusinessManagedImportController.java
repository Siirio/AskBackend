package kz.ask.managedimport.api;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import kz.ask.identity.infrastructure.security.AskPrincipal;
import kz.ask.managedimport.api.dto.CreateManagedImportRequest;
import kz.ask.managedimport.application.ManagedImportProcessor;
import kz.ask.managedimport.domain.dto.ManagedImportDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/businesses/{businessId}/managed-imports")
@RequiredArgsConstructor
public class BusinessManagedImportController {

    private final ManagedImportProcessor managedImportProcessor;

    @PostMapping
    public ResponseEntity<ManagedImportDto> create(
            @AuthenticationPrincipal AskPrincipal principal,
            @PathVariable UUID businessId,
            @Valid @RequestBody CreateManagedImportRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(managedImportProcessor.create(principal, businessId, request));
    }

    @GetMapping
    public ResponseEntity<List<ManagedImportDto>> list(
            @AuthenticationPrincipal AskPrincipal principal,
            @PathVariable UUID businessId) {
        return ResponseEntity.ok(
                managedImportProcessor.listForBusiness(principal, businessId));
    }
}

package kz.ask.managedimport.api;

import java.util.List;
import java.util.UUID;
import kz.ask.identity.infrastructure.security.AskPrincipal;
import kz.ask.managedimport.api.dto.ManagedImportAccessResponse;
import kz.ask.managedimport.application.ManagedImportProcessor;
import kz.ask.managedimport.domain.dto.ManagedImportDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/platform/managed-imports")
@RequiredArgsConstructor
public class PlatformManagedImportController {

    private final ManagedImportProcessor managedImportProcessor;

    @GetMapping
    public ResponseEntity<List<ManagedImportDto>> list(
            @AuthenticationPrincipal AskPrincipal principal) {
        return ResponseEntity.ok(managedImportProcessor.listPlatform(principal));
    }

    @PostMapping("/{requestId}/activate")
    public ResponseEntity<ManagedImportDto> activate(
            @AuthenticationPrincipal AskPrincipal principal,
            @PathVariable UUID requestId) {
        return ResponseEntity.ok(
                managedImportProcessor.activate(principal, requestId));
    }

    @GetMapping("/businesses/{businessId}/items-services-access")
    public ResponseEntity<ManagedImportAccessResponse> itemsServicesAccess(
            @AuthenticationPrincipal AskPrincipal principal,
            @PathVariable UUID businessId) {
        return ResponseEntity.ok(
                managedImportProcessor.itemsServicesAccess(principal, businessId));
    }
}

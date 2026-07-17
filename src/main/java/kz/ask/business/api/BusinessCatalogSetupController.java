package kz.ask.business.api;

import java.util.UUID;
import kz.ask.business.api.dto.BusinessCatalogStatusResponse;
import kz.ask.business.application.BusinessCatalogSetupProcessor;
import kz.ask.identity.infrastructure.security.AskPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/businesses/{businessId}/catalog-setup")
@RequiredArgsConstructor
public class BusinessCatalogSetupController {

    private final BusinessCatalogSetupProcessor processor;

    @GetMapping
    public ResponseEntity<BusinessCatalogStatusResponse> status(
            @AuthenticationPrincipal AskPrincipal principal,
            @PathVariable UUID businessId) {
        return ResponseEntity.ok(processor.status(principal, businessId));
    }

    @PostMapping("/complete")
    public ResponseEntity<BusinessCatalogStatusResponse> complete(
            @AuthenticationPrincipal AskPrincipal principal,
            @PathVariable UUID businessId) {
        return ResponseEntity.ok(processor.complete(principal, businessId));
    }
}

package kz.ask.offer.item.api;

import java.util.UUID;
import kz.ask.offer.item.api.dto.CatalogCapabilitiesResponse;
import kz.ask.offer.item.application.CatalogCapabilityProcessor;
import kz.ask.identity.infrastructure.security.AskPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/businesses/{businessId}/catalog")
@RequiredArgsConstructor
public class CatalogCapabilityController {

    private final CatalogCapabilityProcessor catalogCapabilityProcessor;

    @GetMapping("/capabilities")
    public ResponseEntity<CatalogCapabilitiesResponse> capabilities(
            @AuthenticationPrincipal AskPrincipal principal,
            @PathVariable UUID businessId) {
        return ResponseEntity.ok(catalogCapabilityProcessor.capabilities(principal, businessId));
    }
}

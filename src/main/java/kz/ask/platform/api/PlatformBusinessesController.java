package kz.ask.platform.api;

import java.util.UUID;
import kz.ask.identity.infrastructure.security.AskPrincipal;
import kz.ask.offer.item.api.dto.BusinessProductListResponse;
import kz.ask.offer.service.api.dto.BusinessServiceListResponse;
import kz.ask.platform.api.dto.PlatformBusinessDetailResponse;
import kz.ask.platform.api.dto.PlatformBusinessListResponse;
import kz.ask.platform.application.PlatformBusinessesProcessor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/platform/businesses")
@RequiredArgsConstructor
public class PlatformBusinessesController {

    private final PlatformBusinessesProcessor platformBusinessesProcessor;

    @GetMapping
    public ResponseEntity<PlatformBusinessListResponse> list(
            @AuthenticationPrincipal AskPrincipal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String query) {
        return ResponseEntity.ok(platformBusinessesProcessor.list(principal, page, size, query));
    }

    @GetMapping("/{businessId}")
    public ResponseEntity<PlatformBusinessDetailResponse> detail(
            @AuthenticationPrincipal AskPrincipal principal,
            @PathVariable UUID businessId) {
        return ResponseEntity.ok(platformBusinessesProcessor.detail(principal, businessId));
    }

    @GetMapping("/{businessId}/products")
    public ResponseEntity<BusinessProductListResponse> listItems(
            @AuthenticationPrincipal AskPrincipal principal,
            @PathVariable UUID businessId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return ResponseEntity.ok(
                platformBusinessesProcessor.listItems(principal, businessId, page, size));
    }

    @GetMapping("/{businessId}/services")
    public ResponseEntity<BusinessServiceListResponse> listServices(
            @AuthenticationPrincipal AskPrincipal principal,
            @PathVariable UUID businessId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return ResponseEntity.ok(
                platformBusinessesProcessor.listServices(principal, businessId, page, size));
    }
}

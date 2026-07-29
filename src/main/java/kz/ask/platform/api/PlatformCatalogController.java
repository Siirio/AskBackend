package kz.ask.platform.api;

import kz.ask.identity.infrastructure.security.AskPrincipal;
import kz.ask.platform.api.dto.PlatformCatalogPageResponse;
import kz.ask.platform.application.PlatformCatalogProcessor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/platform/catalog")
@RequiredArgsConstructor
public class PlatformCatalogController {

    private final PlatformCatalogProcessor platformCatalogProcessor;

    @GetMapping("/items")
    public ResponseEntity<PlatformCatalogPageResponse> listItems(
            @AuthenticationPrincipal AskPrincipal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return ResponseEntity.ok(platformCatalogProcessor.listItems(principal, page, size));
    }

    @GetMapping("/services")
    public ResponseEntity<PlatformCatalogPageResponse> listServices(
            @AuthenticationPrincipal AskPrincipal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return ResponseEntity.ok(platformCatalogProcessor.listServices(principal, page, size));
    }

    @GetMapping("/drops")
    public ResponseEntity<PlatformCatalogPageResponse> listDrops(
            @AuthenticationPrincipal AskPrincipal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return ResponseEntity.ok(platformCatalogProcessor.listDrops(principal, page, size));
    }
}

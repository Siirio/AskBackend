package kz.ask.platform.api;

import java.util.UUID;
import kz.ask.identity.infrastructure.security.AskPrincipal;
import kz.ask.platform.api.dto.PlatformBusinessDetailResponse;
import kz.ask.platform.api.dto.PlatformBusinessListResponse;
import kz.ask.platform.api.dto.PlatformProductCreateRequest;
import kz.ask.platform.application.PlatformBusinessProcessor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/platform")
@RequiredArgsConstructor
public class PlatformBusinessController {

    private final PlatformBusinessProcessor processor;

    @GetMapping("/businesses")
    public ResponseEntity<PlatformBusinessListResponse> listBusinesses(
            @AuthenticationPrincipal AskPrincipal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String query) {
        return ResponseEntity.ok(processor.listBusinesses(principal, page, size, query));
    }

    @GetMapping("/businesses/{businessId}")
    public ResponseEntity<PlatformBusinessDetailResponse> detail(
            @AuthenticationPrincipal AskPrincipal principal,
            @PathVariable UUID businessId) {
        return ResponseEntity.ok(processor.detail(principal, businessId));
    }

    @PostMapping("/businesses/{businessId}/products")
    public ResponseEntity<?> createProduct(
            @AuthenticationPrincipal AskPrincipal principal,
            @PathVariable UUID businessId,
            @RequestBody PlatformProductCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(processor.createProduct(principal, businessId, request));
    }

    @DeleteMapping("/products/{productId}")
    public ResponseEntity<Void> deleteProduct(
            @AuthenticationPrincipal AskPrincipal principal,
            @PathVariable UUID productId) {
        processor.deleteProduct(principal, productId);
        return ResponseEntity.noContent().build();
    }
}

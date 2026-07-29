package kz.ask.offer.item.api;

import jakarta.validation.Valid;
import java.util.UUID;
import kz.ask.offer.item.api.dto.BusinessProductCreateRequest;
import kz.ask.offer.item.api.dto.BusinessProductListResponse;
import kz.ask.offer.item.api.dto.BusinessProductRowResponse;
import kz.ask.offer.item.api.dto.BusinessProductUpdateRequest;
import kz.ask.offer.item.application.BusinessProductProcessor;
import kz.ask.identity.infrastructure.security.AskPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class BusinessProductController {

    private final BusinessProductProcessor processor;

    @GetMapping("/api/v1/businesses/{businessId}/items")
    public ResponseEntity<BusinessProductListResponse> listProducts(@AuthenticationPrincipal AskPrincipal principal,
                                                                      @PathVariable UUID businessId,
                                                                      @RequestParam(required = false) UUID branchId,
                                                                      @RequestParam(required = false) Boolean enabled,
                                                                      @RequestParam(required = false) String query,
                                                                      @RequestParam(defaultValue = "0") Integer page,
                                                                      @RequestParam(defaultValue = "20") Integer size) {
        return ResponseEntity.ok(processor.listProducts(principal, businessId, branchId, enabled, query, page, size));
    }

    @PostMapping("/api/v1/businesses/{businessId}/items")
    public ResponseEntity<BusinessProductRowResponse> createProduct(@AuthenticationPrincipal AskPrincipal principal,
                                                                      @PathVariable UUID businessId,
                                                                      @Valid @RequestBody BusinessProductCreateRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(processor.createProduct(principal, businessId, req));
    }

    @PatchMapping("/api/v1/items/{itemId}")
    public ResponseEntity<BusinessProductRowResponse> updateProduct(@AuthenticationPrincipal AskPrincipal principal,
                                                                      @PathVariable UUID itemId,
                                                                      @Valid @RequestBody BusinessProductUpdateRequest req) {
        return ResponseEntity.ok(processor.updateProduct(principal, itemId, req));
    }

    @DeleteMapping("/api/v1/items/{itemId}")
    public ResponseEntity<Void> deleteProduct(@AuthenticationPrincipal AskPrincipal principal,
                                                @PathVariable UUID itemId) {
        processor.deleteProduct(principal, itemId);
        return ResponseEntity.noContent().build();
    }
}

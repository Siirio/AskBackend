package kz.ask.catalog.api;

import jakarta.validation.Valid;
import java.util.UUID;
import kz.ask.catalog.api.dto.BusinessProductCreateRequest;
import kz.ask.catalog.api.dto.BusinessProductListResponse;
import kz.ask.catalog.api.dto.BusinessProductRowResponse;
import kz.ask.catalog.api.dto.BusinessProductUpdateRequest;
import kz.ask.catalog.application.BusinessProductProcessor;
import kz.ask.identity.infrastructure.security.AskPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/business-admin/branches/{branchId}/products")
@RequiredArgsConstructor
public class BusinessProductController {

    private final BusinessProductProcessor processor;

    @GetMapping
    public BusinessProductListResponse listProducts(@AuthenticationPrincipal AskPrincipal principal,
                                                      @PathVariable UUID branchId,
                                                      @RequestParam(required = false) UUID categoryId,
                                                      @RequestParam(required = false) Boolean enabled,
                                                      @RequestParam(required = false) String query,
                                                      @RequestParam(defaultValue = "0") Integer page,
                                                      @RequestParam(defaultValue = "20") Integer size) {
        return processor.listProducts(principal, branchId, categoryId, enabled, query, page, size);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BusinessProductRowResponse createProduct(@AuthenticationPrincipal AskPrincipal principal,
                                                      @PathVariable UUID branchId,
                                                      @Valid @RequestBody BusinessProductCreateRequest req) {
        return processor.createProduct(principal, branchId, req);
    }

    @PatchMapping("/{productId}")
    public BusinessProductRowResponse updateProduct(@AuthenticationPrincipal AskPrincipal principal,
                                                      @PathVariable UUID branchId,
                                                      @PathVariable UUID productId,
                                                      @Valid @RequestBody BusinessProductUpdateRequest req) {
        return processor.updateProduct(principal, branchId, productId, req);
    }

    @DeleteMapping("/{productId}")
    public BusinessProductRowResponse deleteProduct(@AuthenticationPrincipal AskPrincipal principal,
                                                      @PathVariable UUID branchId,
                                                      @PathVariable UUID productId) {
        return processor.deleteProduct(principal, branchId, productId);
    }
}

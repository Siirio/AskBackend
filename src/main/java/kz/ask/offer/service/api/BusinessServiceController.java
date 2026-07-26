package kz.ask.offer.service.api;

import jakarta.validation.Valid;
import java.util.UUID;
import kz.ask.identity.infrastructure.security.AskPrincipal;
import kz.ask.offer.service.api.dto.BusinessServiceCreateRequest;
import kz.ask.offer.service.api.dto.BusinessServiceListResponse;
import kz.ask.offer.service.api.dto.BusinessServiceRowResponse;
import kz.ask.offer.service.api.dto.BusinessServiceUpdateRequest;
import kz.ask.offer.service.application.BusinessServiceProcessor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/businesses/{businessId}/services")
@RequiredArgsConstructor
public class BusinessServiceController {

    private final BusinessServiceProcessor processor;

    @GetMapping
    public ResponseEntity<BusinessServiceListResponse> listServices(@AuthenticationPrincipal AskPrincipal principal,
                                                                     @PathVariable UUID businessId,
                                                                     @RequestParam(required = false) UUID branchId,
                                                                     @RequestParam(required = false) String categoryName,
                                                                     @RequestParam(required = false) Boolean active,
                                                                     @RequestParam(required = false) String query,
                                                                     @RequestParam(defaultValue = "0") Integer page,
                                                                     @RequestParam(defaultValue = "20") Integer size) {
        return ResponseEntity.ok(processor.listServices(principal, businessId, branchId, categoryName, active, query, page, size));
    }

    @PostMapping
    public ResponseEntity<BusinessServiceRowResponse> createService(@AuthenticationPrincipal AskPrincipal principal,
                                                                     @PathVariable UUID businessId,
                                                                     @Valid @RequestBody BusinessServiceCreateRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(processor.createService(principal, businessId, req));
    }

    @PatchMapping("/{serviceOfferingId}")
    public ResponseEntity<BusinessServiceRowResponse> updateService(@AuthenticationPrincipal AskPrincipal principal,
                                                                     @PathVariable UUID businessId,
                                                                     @PathVariable UUID serviceOfferingId,
                                                                     @Valid @RequestBody BusinessServiceUpdateRequest req) {
        return ResponseEntity.ok(processor.updateService(principal, businessId, serviceOfferingId, req));
    }

    @DeleteMapping("/{serviceOfferingId}")
    public ResponseEntity<Void> deleteService(@AuthenticationPrincipal AskPrincipal principal,
                                              @PathVariable UUID businessId,
                                              @PathVariable UUID serviceOfferingId) {
        processor.deleteService(principal, businessId, serviceOfferingId);
        return ResponseEntity.noContent().build();
    }
}

package kz.ask.service.api;

import jakarta.validation.Valid;
import java.util.UUID;
import kz.ask.identity.infrastructure.security.AskPrincipal;
import kz.ask.service.api.dto.BusinessServiceCreateRequest;
import kz.ask.service.api.dto.BusinessServiceListResponse;
import kz.ask.service.api.dto.BusinessServiceRowResponse;
import kz.ask.service.api.dto.BusinessServiceUpdateRequest;
import kz.ask.service.application.BusinessServiceProcessor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/business-admin/branches/{branchId}/services")
@RequiredArgsConstructor
public class BusinessServiceController {

    private final BusinessServiceProcessor processor;

    @GetMapping
    public ResponseEntity<BusinessServiceListResponse> listServices(@AuthenticationPrincipal AskPrincipal principal,
                                                                     @PathVariable UUID branchId,
                                                                     @RequestParam(required = false) String categoryLabel,
                                                                     @RequestParam(required = false) Boolean active,
                                                                     @RequestParam(required = false) String query,
                                                                     @RequestParam(defaultValue = "0") Integer page,
                                                                     @RequestParam(defaultValue = "20") Integer size) {
        return ResponseEntity.ok(processor.listServices(principal, branchId, categoryLabel, active, query, page, size));
    }

    @PostMapping
    public ResponseEntity<BusinessServiceRowResponse> createService(@AuthenticationPrincipal AskPrincipal principal,
                                                                     @PathVariable UUID branchId,
                                                                     @Valid @RequestBody BusinessServiceCreateRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(processor.createService(principal, branchId, req));
    }

    @PatchMapping("/{serviceOfferingId}")
    public ResponseEntity<BusinessServiceRowResponse> updateService(@AuthenticationPrincipal AskPrincipal principal,
                                                                     @PathVariable UUID branchId,
                                                                     @PathVariable UUID serviceOfferingId,
                                                                     @Valid @RequestBody BusinessServiceUpdateRequest req) {
        return ResponseEntity.ok(processor.updateService(principal, branchId, serviceOfferingId, req));
    }
}

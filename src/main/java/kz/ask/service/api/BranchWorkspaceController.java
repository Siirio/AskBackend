package kz.ask.service.api;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import kz.ask.identity.infrastructure.security.AskPrincipal;
import kz.ask.service.api.dto.ActivityRowResponse;
import kz.ask.service.api.dto.BusinessServiceListResponse;
import kz.ask.service.api.dto.BusinessServiceRowResponse;
import kz.ask.service.api.dto.CreateServiceRequest;
import kz.ask.service.api.dto.FixServiceBookingRequest;
import kz.ask.service.api.dto.FixServiceBookingResponse;
import kz.ask.service.api.dto.UpdateServiceRequest;
import kz.ask.service.application.processor.ServiceProcessor;
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
@RequestMapping("/api/v1/business-admin/branches/{branchId}")
@RequiredArgsConstructor
public class BranchWorkspaceController {

    private final ServiceProcessor serviceProcessor;

    @GetMapping("/services")
    public ResponseEntity<BusinessServiceListResponse> listServices(
            @AuthenticationPrincipal AskPrincipal principal,
            @PathVariable UUID branchId,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) String query,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        return ResponseEntity.ok(
                serviceProcessor.listServices(principal, branchId, categoryId, active, query, page, size));
    }

    @PostMapping("/services")
    public ResponseEntity<BusinessServiceRowResponse> createService(
            @AuthenticationPrincipal AskPrincipal principal,
            @PathVariable UUID branchId,
            @Valid @RequestBody CreateServiceRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(serviceProcessor.createService(principal, branchId, request));
    }

    @PatchMapping("/services/{serviceOfferingId}")
    public ResponseEntity<BusinessServiceRowResponse> updateService(
            @AuthenticationPrincipal AskPrincipal principal,
            @PathVariable UUID branchId,
            @PathVariable UUID serviceOfferingId,
            @RequestBody UpdateServiceRequest request) {
        return ResponseEntity.ok(
                serviceProcessor.updateService(principal, branchId, serviceOfferingId, request));
    }

    @GetMapping("/activity")
    public ResponseEntity<List<ActivityRowResponse>> listActivity(
            @AuthenticationPrincipal AskPrincipal principal,
            @PathVariable UUID branchId) {
        return ResponseEntity.ok(serviceProcessor.listActivity(principal, branchId));
    }

    @PatchMapping("/service-requests/{requestId}")
    public ResponseEntity<FixServiceBookingResponse> fixServiceBooking(
            @AuthenticationPrincipal AskPrincipal principal,
            @PathVariable UUID branchId,
            @PathVariable UUID requestId,
            @Valid @RequestBody FixServiceBookingRequest request) {
        return ResponseEntity.ok(
                serviceProcessor.fixServiceBooking(principal, branchId, requestId, request));
    }
}

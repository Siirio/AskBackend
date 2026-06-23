package kz.ask.service.api;

import jakarta.validation.Valid;
import kz.ask.identity.infrastructure.security.AskPrincipal;
import kz.ask.service.api.dto.*;
import kz.ask.service.application.processor.ServiceProcessor;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/business-admin/branches/{branchId}")
@RequiredArgsConstructor
public class BranchWorkspaceController {

    private final ServiceProcessor serviceProcessor;

    @GetMapping("/services")
    public Page<BusinessServiceRowResponse> listServices(
            @AuthenticationPrincipal AskPrincipal principal,
            @PathVariable UUID branchId,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return serviceProcessor.listServices(principal, branchId, categoryId, active, query, page, size);
    }

    @PostMapping("/services")
    @ResponseStatus(HttpStatus.CREATED)
    public BusinessServiceRowResponse createService(
            @AuthenticationPrincipal AskPrincipal principal,
            @PathVariable UUID branchId,
            @RequestBody @Valid CreateServiceRequest request) {
        return serviceProcessor.createService(principal, branchId, request);
    }

    @PatchMapping("/services/{serviceOfferingId}")
    public BusinessServiceRowResponse updateService(
            @AuthenticationPrincipal AskPrincipal principal,
            @PathVariable UUID branchId,
            @PathVariable UUID serviceOfferingId,
            @RequestBody UpdateServiceRequest request) {
        return serviceProcessor.updateService(principal, branchId, serviceOfferingId, request);
    }

    @PatchMapping("/service-requests/{requestId}")
    public ServiceRequestResponse handleServiceRequest(
            @AuthenticationPrincipal AskPrincipal principal,
            @PathVariable UUID branchId,
            @PathVariable UUID requestId,
            @RequestBody @Valid HandleServiceRequestBody body) {
        return serviceProcessor.handleServiceRequest(principal, branchId, requestId, body);
    }
}

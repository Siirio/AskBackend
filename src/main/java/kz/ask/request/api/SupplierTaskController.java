package kz.ask.request.api;

import java.util.List;
import java.util.UUID;
import kz.ask.identity.infrastructure.security.AskPrincipal;
import kz.ask.request.api.dto.SupplierRespondRequest;
import kz.ask.request.api.dto.SupplierTaskDetailResponse;
import kz.ask.request.api.dto.SupplierTaskResponse;
import kz.ask.request.application.processor.SupplierTaskProcessor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/business-admin/branches/{branchId}/tasks")
@RequiredArgsConstructor
public class SupplierTaskController {

    private final SupplierTaskProcessor processor;

    @GetMapping
    public ResponseEntity<List<SupplierTaskResponse>> listTasks(@AuthenticationPrincipal AskPrincipal principal,
                                                                   @PathVariable UUID branchId) {
        return ResponseEntity.ok(processor.listTasks(principal, branchId));
    }

    @GetMapping("/{taskId}")
    public ResponseEntity<SupplierTaskDetailResponse> getDetail(@AuthenticationPrincipal AskPrincipal principal,
                                                                  @PathVariable UUID branchId,
                                                                  @PathVariable UUID taskId) {
        return ResponseEntity.ok(processor.getDetail(principal, branchId, taskId));
    }

    @PostMapping("/{taskId}/respond")
    public ResponseEntity<SupplierTaskDetailResponse> respond(@AuthenticationPrincipal AskPrincipal principal,
                                                                @PathVariable UUID branchId,
                                                                @PathVariable UUID taskId,
                                                                @RequestBody SupplierRespondRequest req) {
        return ResponseEntity.ok(processor.respond(principal, branchId, taskId, req));
    }
}

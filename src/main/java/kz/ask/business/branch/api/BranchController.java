package kz.ask.business.branch.api;

import jakarta.validation.Valid;
import java.util.UUID;
import kz.ask.business.branch.api.dto.BranchListResponse;
import kz.ask.business.branch.api.dto.BranchResponse;
import kz.ask.business.branch.api.dto.CreateBranchRequest;
import kz.ask.business.branch.api.dto.UpdateBranchRequest;
import kz.ask.business.branch.application.BranchManagementProcessor;
import kz.ask.identity.infrastructure.security.AskPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class BranchController {

    private final BranchManagementProcessor branchProcessor;

    @PostMapping("/api/v1/businesses/{businessId}/branches")
    public ResponseEntity<BranchResponse> create(@AuthenticationPrincipal AskPrincipal principal,
                                                  @PathVariable UUID businessId,
                                                  @Valid @RequestBody CreateBranchRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(branchProcessor.createBranch(principal, businessId, req));
    }

    @GetMapping("/api/v1/businesses/{businessId}/branches")
    public ResponseEntity<BranchListResponse> list(@AuthenticationPrincipal AskPrincipal principal,
                                                      @PathVariable UUID businessId) {
        return ResponseEntity.ok(branchProcessor.listBranches(principal, businessId));
    }

    @PatchMapping("/api/v1/branches/{branchId}")
    public ResponseEntity<BranchResponse> update(@AuthenticationPrincipal AskPrincipal principal,
                                                  @PathVariable UUID branchId,
                                                  @Valid @RequestBody UpdateBranchRequest req) {
        return ResponseEntity.ok(branchProcessor.updateBranch(principal, branchId, req));
    }
}

package kz.ask.business.api;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import kz.ask.business.api.dto.BranchResponse;
import kz.ask.business.api.dto.CreateBranchRequest;
import kz.ask.business.api.dto.UpdateBranchRequest;
import kz.ask.business.application.BranchManagementProcessor;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/businesses/{businessId}/branches")
@RequiredArgsConstructor
public class BranchController {

    private final BranchManagementProcessor branchProcessor;

    @PostMapping
    public ResponseEntity<BranchResponse> create(@AuthenticationPrincipal AskPrincipal principal,
                                                  @PathVariable UUID businessId,
                                                  @Valid @RequestBody CreateBranchRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(branchProcessor.createBranch(principal, businessId, req));
    }

    @GetMapping
    public ResponseEntity<List<BranchResponse>> list(@AuthenticationPrincipal AskPrincipal principal,
                                                      @PathVariable UUID businessId) {
        return ResponseEntity.ok(branchProcessor.listBranches(principal, businessId));
    }

    @PatchMapping("/{branchId}")
    public ResponseEntity<BranchResponse> update(@AuthenticationPrincipal AskPrincipal principal,
                                                  @PathVariable UUID businessId,
                                                  @PathVariable UUID branchId,
                                                  @Valid @RequestBody UpdateBranchRequest req) {
        return ResponseEntity.ok(branchProcessor.updateBranch(principal, businessId, branchId, req));
    }
}

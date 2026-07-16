package kz.ask.business.api;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import kz.ask.business.api.dto.CreateEmployeeRequest;
import kz.ask.business.api.dto.CreateStaffRequest;
import kz.ask.business.api.dto.StaffResponse;
import kz.ask.business.api.dto.UpdateStaffRequest;
import kz.ask.business.application.StaffManagementProcessor;
import kz.ask.identity.infrastructure.security.AskPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class StaffController {

    private final StaffManagementProcessor staffProcessor;

    @PostMapping("/api/v1/businesses/{businessId}/branches/{branchId}/staff")
    public ResponseEntity<StaffResponse> createStaff(@AuthenticationPrincipal AskPrincipal principal,
                                      @PathVariable UUID businessId,
                                      @PathVariable UUID branchId,
                                      @Valid @RequestBody CreateStaffRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(staffProcessor.createStaff(principal, businessId, branchId, req));
    }

    @GetMapping("/api/v1/businesses/{businessId}/branches/{branchId}/staff")
    public ResponseEntity<List<StaffResponse>> listStaff(@AuthenticationPrincipal AskPrincipal principal,
                                          @PathVariable UUID businessId,
                                          @PathVariable UUID branchId) {
        return ResponseEntity.ok(staffProcessor.listStaff(principal, businessId, branchId));
    }

    @PostMapping("/api/v1/businesses/{businessId}/branches/{branchId}/staff/{staffId}/update")
    public ResponseEntity<StaffResponse> updateStaff(@AuthenticationPrincipal AskPrincipal principal,
                                      @PathVariable UUID businessId,
                                      @PathVariable UUID branchId,
                                      @PathVariable UUID staffId,
                                      @Valid @RequestBody UpdateStaffRequest req) {
        return ResponseEntity.ok(staffProcessor.updateStaff(principal, businessId, branchId, staffId, req));
    }

    @PostMapping("/api/v1/businesses/{businessId}/branches/{branchId}/staff/{staffId}/reset-password")
    public ResponseEntity<StaffResponse> resetPassword(@AuthenticationPrincipal AskPrincipal principal,
                                        @PathVariable UUID businessId,
                                        @PathVariable UUID branchId,
                                        @PathVariable UUID staffId) {
        return ResponseEntity.ok(staffProcessor.resetPassword(principal, businessId, branchId, staffId));
    }

    @PostMapping("/api/v1/businesses/{businessId}/staff")
    public ResponseEntity<StaffResponse> createEmployee(@AuthenticationPrincipal AskPrincipal principal,
                                          @PathVariable UUID businessId,
                                          @Valid @RequestBody CreateEmployeeRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(staffProcessor.createEmployee(principal, businessId, req));
    }

    @GetMapping("/api/v1/businesses/{businessId}/staff")
    public ResponseEntity<List<StaffResponse>> listEmployees(@AuthenticationPrincipal AskPrincipal principal,
                                               @PathVariable UUID businessId) {
        return ResponseEntity.ok(staffProcessor.listEmployees(principal, businessId));
    }
}

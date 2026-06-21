package kz.ask.business.api;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import kz.ask.business.api.dto.CreateStaffRequest;
import kz.ask.business.api.dto.StaffResponse;
import kz.ask.business.api.dto.UpdateStaffRequest;
import kz.ask.business.application.StaffManagementProcessor;
import kz.ask.identity.infrastructure.security.AskPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/businesses/{businessId}/branches/{branchId}/staff")
@RequiredArgsConstructor
public class StaffController {

    private final StaffManagementProcessor staffProcessor;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public StaffResponse createStaff(@AuthenticationPrincipal AskPrincipal principal,
                                      @PathVariable UUID businessId,
                                      @PathVariable UUID branchId,
                                      @Valid @RequestBody CreateStaffRequest req) {
        return staffProcessor.createStaff(principal, businessId, branchId, req);
    }

    @GetMapping
    public List<StaffResponse> listStaff(@AuthenticationPrincipal AskPrincipal principal,
                                          @PathVariable UUID businessId,
                                          @PathVariable UUID branchId) {
        return staffProcessor.listStaff(principal, businessId, branchId);
    }

    @PostMapping("/{staffId}/update")
    public StaffResponse updateStaff(@AuthenticationPrincipal AskPrincipal principal,
                                      @PathVariable UUID businessId,
                                      @PathVariable UUID branchId,
                                      @PathVariable UUID staffId,
                                      @Valid @RequestBody UpdateStaffRequest req) {
        return staffProcessor.updateStaff(principal, businessId, branchId, staffId, req);
    }

    @PostMapping("/{staffId}/reset-password")
    public StaffResponse resetPassword(@AuthenticationPrincipal AskPrincipal principal,
                                        @PathVariable UUID businessId,
                                        @PathVariable UUID branchId,
                                        @PathVariable UUID staffId) {
        return staffProcessor.resetPassword(principal, businessId, branchId, staffId);
    }
}

package kz.ask.business.api;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import kz.ask.business.api.dto.CreateInviteRequest;
import kz.ask.business.api.dto.InviteResponse;
import kz.ask.business.application.InviteProcessor;
import kz.ask.identity.infrastructure.security.AskPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/businesses/{businessId}/branches/{branchId}/invites")
@RequiredArgsConstructor
public class InviteController {

    private final InviteProcessor inviteProcessor;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public InviteResponse createInvite(@AuthenticationPrincipal AskPrincipal principal,
                                        @PathVariable UUID businessId,
                                        @PathVariable UUID branchId,
                                        @Valid @RequestBody CreateInviteRequest req) {
        return inviteProcessor.createInvite(principal, businessId, branchId, req);
    }

    @GetMapping
    public List<InviteResponse> listInvites(@AuthenticationPrincipal AskPrincipal principal,
                                             @PathVariable UUID businessId,
                                             @PathVariable UUID branchId) {
        return inviteProcessor.listInvites(principal, businessId, branchId);
    }

    @DeleteMapping("/{inviteId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void revokeInvite(@AuthenticationPrincipal AskPrincipal principal,
                              @PathVariable UUID businessId,
                              @PathVariable UUID branchId,
                              @PathVariable UUID inviteId) {
        inviteProcessor.revokeInvite(principal, businessId, branchId, inviteId);
    }
}

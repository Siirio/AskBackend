package kz.ask.business.invitation.api;

import jakarta.validation.Valid;
import java.util.UUID;
import kz.ask.business.invitation.api.dto.BusinessInvitationListResponse;
import kz.ask.business.invitation.api.dto.BusinessInvitationResponse;
import kz.ask.business.invitation.api.dto.CreateBusinessInvitationRequest;
import kz.ask.business.invitation.application.BusinessInvitationProcessor;
import kz.ask.identity.infrastructure.security.AskPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class BusinessInvitationController {

    private final BusinessInvitationProcessor businessInvitationProcessor;

    @PostMapping("/api/v1/businesses/{businessId}/invitations")
    public ResponseEntity<BusinessInvitationResponse> create(
            @AuthenticationPrincipal AskPrincipal principal,
            @PathVariable UUID businessId,
            @Valid @RequestBody CreateBusinessInvitationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(businessInvitationProcessor.create(principal, businessId, request));
    }

    @GetMapping("/api/v1/businesses/{businessId}/invitations")
    public ResponseEntity<BusinessInvitationListResponse> list(
            @AuthenticationPrincipal AskPrincipal principal,
            @PathVariable UUID businessId) {
        return ResponseEntity.ok(
                businessInvitationProcessor.listBusiness(principal, businessId));
    }

    @DeleteMapping("/api/v1/invitations/{invitationId}")
    public ResponseEntity<Void> revoke(
            @AuthenticationPrincipal AskPrincipal principal,
            @PathVariable UUID invitationId) {
        businessInvitationProcessor.revoke(principal, invitationId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/api/v1/me/invitations")
    public ResponseEntity<BusinessInvitationListResponse> listMine(
            @AuthenticationPrincipal AskPrincipal principal) {
        return ResponseEntity.ok(businessInvitationProcessor.listMine(principal));
    }

    @PostMapping("/api/v1/me/invitations/{invitationId}/accept")
    public ResponseEntity<BusinessInvitationResponse> accept(
            @AuthenticationPrincipal AskPrincipal principal,
            @PathVariable UUID invitationId) {
        return ResponseEntity.ok(
                businessInvitationProcessor.accept(principal, invitationId));
    }

    @PostMapping("/api/v1/me/invitations/{invitationId}/decline")
    public ResponseEntity<BusinessInvitationResponse> decline(
            @AuthenticationPrincipal AskPrincipal principal,
            @PathVariable UUID invitationId) {
        return ResponseEntity.ok(
                businessInvitationProcessor.decline(principal, invitationId));
    }
}

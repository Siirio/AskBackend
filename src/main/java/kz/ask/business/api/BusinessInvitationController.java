package kz.ask.business.api;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import kz.ask.business.api.dto.BusinessInvitationResponse;
import kz.ask.business.api.dto.CreateBusinessInvitationRequest;
import kz.ask.business.application.BusinessInvitationProcessor;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/businesses/{businessId}/invitations")
@RequiredArgsConstructor
public class BusinessInvitationController {

    private final BusinessInvitationProcessor businessInvitationProcessor;

    @PostMapping
    public ResponseEntity<BusinessInvitationResponse> create(
            @AuthenticationPrincipal AskPrincipal principal,
            @PathVariable UUID businessId,
            @Valid @RequestBody CreateBusinessInvitationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(businessInvitationProcessor.create(principal, businessId, request));
    }

    @GetMapping
    public ResponseEntity<List<BusinessInvitationResponse>> list(
            @AuthenticationPrincipal AskPrincipal principal,
            @PathVariable UUID businessId) {
        return ResponseEntity.ok(
                businessInvitationProcessor.listBusiness(principal, businessId));
    }

    @DeleteMapping("/{invitationId}")
    public ResponseEntity<Void> revoke(
            @AuthenticationPrincipal AskPrincipal principal,
            @PathVariable UUID businessId,
            @PathVariable UUID invitationId) {
        businessInvitationProcessor.revoke(principal, businessId, invitationId);
        return ResponseEntity.noContent().build();
    }
}

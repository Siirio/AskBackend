package kz.ask.business.api;

import java.util.List;
import java.util.UUID;
import kz.ask.business.api.dto.BusinessInvitationResponse;
import kz.ask.business.application.BusinessInvitationProcessor;
import kz.ask.identity.infrastructure.security.AskPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/me/invitations")
@RequiredArgsConstructor
public class MyBusinessInvitationController {

    private final BusinessInvitationProcessor businessInvitationProcessor;

    @GetMapping
    public ResponseEntity<List<BusinessInvitationResponse>> listMine(
            @AuthenticationPrincipal AskPrincipal principal) {
        return ResponseEntity.ok(businessInvitationProcessor.listMine(principal));
    }

    @PostMapping("/{invitationId}/accept")
    public ResponseEntity<BusinessInvitationResponse> accept(
            @AuthenticationPrincipal AskPrincipal principal,
            @PathVariable UUID invitationId) {
        return ResponseEntity.ok(
                businessInvitationProcessor.accept(principal, invitationId));
    }

    @PostMapping("/{invitationId}/decline")
    public ResponseEntity<BusinessInvitationResponse> decline(
            @AuthenticationPrincipal AskPrincipal principal,
            @PathVariable UUID invitationId) {
        return ResponseEntity.ok(
                businessInvitationProcessor.decline(principal, invitationId));
    }
}

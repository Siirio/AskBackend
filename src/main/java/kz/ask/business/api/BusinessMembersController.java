package kz.ask.business.api;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import kz.ask.business.api.dto.UpdateBusinessMemberRequest;
import kz.ask.business.application.BusinessMembersProcessor;
import kz.ask.business.domain.dto.BusinessMemberDto;
import kz.ask.identity.infrastructure.security.AskPrincipal;
import lombok.RequiredArgsConstructor;
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
@RequestMapping("/api/v1/businesses/{businessId}/members")
@RequiredArgsConstructor
public class BusinessMembersController {

    private final BusinessMembersProcessor businessMembersProcessor;

    @GetMapping
    public ResponseEntity<List<BusinessMemberDto>> list(
            @AuthenticationPrincipal AskPrincipal principal,
            @PathVariable UUID businessId) {
        return ResponseEntity.ok(businessMembersProcessor.list(principal, businessId));
    }

    @PatchMapping("/{membershipId}")
    public ResponseEntity<BusinessMemberDto> updateRole(
            @AuthenticationPrincipal AskPrincipal principal,
            @PathVariable UUID businessId,
            @PathVariable UUID membershipId,
            @Valid @RequestBody UpdateBusinessMemberRequest request) {
        return ResponseEntity.ok(
                businessMembersProcessor.updateRole(principal, businessId, membershipId, request));
    }

    @PostMapping("/{membershipId}/deactivate")
    public ResponseEntity<BusinessMemberDto> deactivate(
            @AuthenticationPrincipal AskPrincipal principal,
            @PathVariable UUID businessId,
            @PathVariable UUID membershipId) {
        return ResponseEntity.ok(
                businessMembersProcessor.deactivate(principal, businessId, membershipId));
    }
}

package kz.ask.business.member.api;

import jakarta.validation.Valid;
import java.util.UUID;
import kz.ask.business.member.api.dto.BusinessMemberListResponse;
import kz.ask.business.member.api.dto.UpdateBusinessMemberRequest;
import kz.ask.business.member.application.BusinessMembersProcessor;
import kz.ask.business.member.domain.dto.BusinessMemberDto;
import kz.ask.identity.infrastructure.security.AskPrincipal;
import lombok.RequiredArgsConstructor;
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
public class BusinessMembersController {

    private final BusinessMembersProcessor businessMembersProcessor;

    @GetMapping("/api/v1/businesses/{businessId}/members")
    public ResponseEntity<BusinessMemberListResponse> list(
            @AuthenticationPrincipal AskPrincipal principal,
            @PathVariable UUID businessId) {
        return ResponseEntity.ok(businessMembersProcessor.list(principal, businessId));
    }

    @PatchMapping("/api/v1/members/{membershipId}")
    public ResponseEntity<BusinessMemberDto> updateRole(
            @AuthenticationPrincipal AskPrincipal principal,
            @PathVariable UUID membershipId,
            @Valid @RequestBody UpdateBusinessMemberRequest request) {
        return ResponseEntity.ok(
                businessMembersProcessor.updateRole(principal, membershipId, request));
    }

    @PostMapping("/api/v1/members/{membershipId}/deactivate")
    public ResponseEntity<BusinessMemberDto> deactivate(
            @AuthenticationPrincipal AskPrincipal principal,
            @PathVariable UUID membershipId) {
        return ResponseEntity.ok(
                businessMembersProcessor.deactivate(principal, membershipId));
    }
}

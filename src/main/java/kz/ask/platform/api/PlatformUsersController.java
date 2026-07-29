package kz.ask.platform.api;

import io.swagger.v3.oas.annotations.Hidden;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import kz.ask.identity.infrastructure.security.AskPrincipal;
import kz.ask.platform.api.dto.CreateAdminRequest;
import kz.ask.platform.api.dto.CreatePlatformUserRequest;
import kz.ask.platform.api.dto.UpdatePlatformUserRequest;
import kz.ask.platform.application.PlatformUsersProcessor;
import kz.ask.platform.domain.dto.PlatformMembershipDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/platform/users")
@RequiredArgsConstructor
public class PlatformUsersController {

    private final PlatformUsersProcessor platformUsersProcessor;

    @GetMapping
    public ResponseEntity<List<PlatformMembershipDto>> list(
            @AuthenticationPrincipal AskPrincipal principal) {
        return ResponseEntity.ok(platformUsersProcessor.list(principal));
    }

    @PostMapping
    public ResponseEntity<PlatformMembershipDto> create(
            @AuthenticationPrincipal AskPrincipal principal,
            @Valid @RequestBody CreatePlatformUserRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(platformUsersProcessor.create(principal, request));
    }

    @PatchMapping("/{membershipId}")
    public ResponseEntity<PlatformMembershipDto> update(
            @AuthenticationPrincipal AskPrincipal principal,
            @PathVariable UUID membershipId,
            @Valid @RequestBody UpdatePlatformUserRequest request) {
        return ResponseEntity.ok(platformUsersProcessor.update(principal, membershipId, request));
    }

    @PostMapping("/{membershipId}/deactivate")
    public ResponseEntity<PlatformMembershipDto> deactivate(
            @AuthenticationPrincipal AskPrincipal principal,
            @PathVariable UUID membershipId) {
        return ResponseEntity.ok(platformUsersProcessor.deactivate(principal, membershipId));
    }

    @Hidden
    @PostMapping("/admin")
    public ResponseEntity<PlatformMembershipDto> createAdmin(
            @AuthenticationPrincipal AskPrincipal principal,
            @Valid @RequestBody CreateAdminRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(platformUsersProcessor.createAdmin(principal, request));
    }

    @Hidden
    @DeleteMapping("/{membershipId}")
    public ResponseEntity<Void> deleteAdmin(
            @AuthenticationPrincipal AskPrincipal principal,
            @PathVariable UUID membershipId) {
        platformUsersProcessor.deleteAdmin(principal, membershipId);
        return ResponseEntity.noContent().build();
    }
}

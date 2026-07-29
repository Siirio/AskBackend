package kz.ask.platform.api;

import jakarta.validation.Valid;
import java.util.UUID;
import kz.ask.identity.domain.enums.UserStatus;
import kz.ask.identity.infrastructure.security.AskPrincipal;
import kz.ask.platform.api.dto.DeletePlatformAccountRequest;
import kz.ask.platform.api.dto.PlatformAccountListResponse;
import kz.ask.platform.application.PlatformAccountsProcessor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/platform/accounts")
@RequiredArgsConstructor
public class PlatformAccountsController {

    private final PlatformAccountsProcessor platformAccountsProcessor;

    @GetMapping
    public ResponseEntity<PlatformAccountListResponse> list(
            @AuthenticationPrincipal AskPrincipal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "30") int size,
            @RequestParam(required = false) String query,
            @RequestParam(required = false) UserStatus status) {
        return ResponseEntity.ok(platformAccountsProcessor.list(principal, page, size, query, status));
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal AskPrincipal principal,
            @PathVariable UUID userId,
            @Valid @RequestBody DeletePlatformAccountRequest request) {
        platformAccountsProcessor.delete(principal, userId, request.getReason());
        return ResponseEntity.noContent().build();
    }
}

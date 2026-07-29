package kz.ask.platform.api;

import kz.ask.identity.infrastructure.security.AskPrincipal;
import kz.ask.platform.api.dto.PlatformDashboardResponse;
import kz.ask.platform.application.PlatformDashboardProcessor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/platform/dashboard")
@RequiredArgsConstructor
public class PlatformDashboardController {

    private final PlatformDashboardProcessor platformDashboardProcessor;

    @GetMapping
    public ResponseEntity<PlatformDashboardResponse> get(
            @AuthenticationPrincipal AskPrincipal principal) {
        return ResponseEntity.ok(platformDashboardProcessor.get(principal));
    }
}

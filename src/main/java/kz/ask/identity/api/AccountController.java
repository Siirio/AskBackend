package kz.ask.identity.api;

import kz.ask.identity.api.dto.LogoutResponse;
import kz.ask.identity.application.AccountLifecycleProcessor;
import kz.ask.identity.infrastructure.security.AskPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/account")
@RequiredArgsConstructor
public class AccountController {

    private final AccountLifecycleProcessor accountLifecycleProcessor;

    @DeleteMapping
    public ResponseEntity<LogoutResponse> delete(
            @AuthenticationPrincipal AskPrincipal principal) {
        return ResponseEntity.ok(accountLifecycleProcessor.delete(principal));
    }
}

package kz.ask.identity.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kz.ask.identity.api.dto.AuthChallengeResponse;
import kz.ask.identity.api.dto.AuthSessionResponse;
import kz.ask.identity.api.dto.BusinessLoginStartRequest;
import kz.ask.identity.api.dto.BusinessRegisterRequest;
import kz.ask.identity.api.dto.ChangeTemporaryPasswordRequest;
import kz.ask.identity.api.dto.CustomerLoginStartRequest;
import kz.ask.identity.api.dto.CustomerRegisterRequest;
import kz.ask.identity.api.dto.LoginRequest;
import kz.ask.identity.api.dto.LogoutResponse;
import kz.ask.identity.api.dto.UpdateProfileRequest;
import kz.ask.identity.api.dto.VerifyCodeRequest;
import kz.ask.identity.application.AuthProcessor;
import kz.ask.identity.application.LoginProcessor;
import kz.ask.identity.infrastructure.security.AskPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Auth", description = "Customer and business authentication, registration and session endpoints")
@RequiredArgsConstructor
public class AuthController {

    private final AuthProcessor authProcessor;
    private final LoginProcessor loginProcessor;

    @PostMapping("/login")
    public ResponseEntity<AuthSessionResponse> login(@Valid @RequestBody LoginRequest req) {
        return ResponseEntity.ok(loginProcessor.login(req));
    }

    @PostMapping("/change-temporary-password")
    public ResponseEntity<AuthSessionResponse> changeTemporaryPassword(@AuthenticationPrincipal AskPrincipal principal,
                                                        @Valid @RequestBody ChangeTemporaryPasswordRequest req) {
        return ResponseEntity.ok(loginProcessor.changeTemporaryPassword(principal, req));
    }

    @Operation(summary = "Start customer login", description = "Issues a verification challenge for an existing customer account")
    @PostMapping("/customer/login/start")
    public ResponseEntity<AuthChallengeResponse> startCustomerLogin(@Valid @RequestBody CustomerLoginStartRequest req) {
        return ResponseEntity.ok(authProcessor.startCustomerLogin(req));
    }

    @Operation(summary = "Register customer", description = "Creates a new customer account and issues a verification challenge")
    @PostMapping("/customer/register")
    public ResponseEntity<AuthChallengeResponse> registerCustomer(@Valid @RequestBody CustomerRegisterRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authProcessor.registerCustomer(req));
    }

    @Operation(summary = "Start business login", description = "Issues a verification challenge for an existing business account")
    @PostMapping("/business/login/start")
    public ResponseEntity<AuthChallengeResponse> startBusinessLogin(@Valid @RequestBody BusinessLoginStartRequest req) {
        return ResponseEntity.ok(authProcessor.startBusinessLogin(req));
    }

    @Operation(summary = "Register business", description = "Creates a new business account and issues a verification challenge")
    @PostMapping("/business/register")
    public ResponseEntity<AuthChallengeResponse> registerBusiness(@Valid @RequestBody BusinessRegisterRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authProcessor.registerBusiness(req));
    }

    @Operation(summary = "Verify challenge code", description = "Confirms a login or registration challenge and returns an authenticated session")
    @PostMapping("/verify")
    public ResponseEntity<AuthSessionResponse> verifyCode(@Valid @RequestBody VerifyCodeRequest req) {
        return ResponseEntity.ok(authProcessor.verifyCode(req));
    }

    @Operation(summary = "Get current session", description = "Returns the authenticated session for the current principal")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/session")
    public ResponseEntity<AuthSessionResponse> currentSession(@AuthenticationPrincipal AskPrincipal principal) {
        return ResponseEntity.ok(authProcessor.currentSession(principal));
    }

    @Operation(summary = "Logout", description = "Invalidates the current authenticated session")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/logout")
    public ResponseEntity<LogoutResponse> logout(@AuthenticationPrincipal AskPrincipal principal) {
        return ResponseEntity.ok(authProcessor.logout(principal));
    }

    @Operation(summary = "Update profile", description = "Updates the display name, email, or phone of the authenticated user")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/profile")
    public ResponseEntity<AuthSessionResponse> updateProfile(@AuthenticationPrincipal AskPrincipal principal,
                                                              @Valid @RequestBody UpdateProfileRequest req) {
        return ResponseEntity.ok(authProcessor.updateProfile(principal, req));
    }
}

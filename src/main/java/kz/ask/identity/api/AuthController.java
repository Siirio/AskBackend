package kz.ask.identity.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletResponse;
import kz.ask.identity.api.dto.VerificationResponse;
import kz.ask.identity.api.dto.AuthSessionResponse;
import kz.ask.identity.api.dto.BusinessLoginStartRequest;
import kz.ask.identity.api.dto.BusinessRegisterRequest;
import kz.ask.identity.api.dto.CancelVerificationRequest;
import kz.ask.identity.api.dto.ChangePasswordRequest;
import kz.ask.identity.api.dto.ChangeTemporaryPasswordRequest;
import kz.ask.identity.api.dto.CustomerLoginStartRequest;
import kz.ask.identity.api.dto.CustomerRegisterRequest;
import kz.ask.identity.api.dto.LoginRequest;
import kz.ask.identity.api.dto.LogoutResponse;
import kz.ask.identity.api.dto.RequestEmailChangeRequest;
import kz.ask.identity.api.dto.TwoFactorChangeRequest;
import kz.ask.identity.api.dto.UpdateProfileRequest;
import kz.ask.identity.api.dto.VerifyCodeRequest;
import kz.ask.identity.application.AuthProcessor;
import kz.ask.identity.application.LoginProcessor;
import kz.ask.identity.infrastructure.security.AskPrincipal;
import kz.ask.identity.infrastructure.security.AuthCookieService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Auth", description = "Customer and business authentication, registration and session endpoints")
@RequiredArgsConstructor
public class AuthController {

    private final AuthProcessor authProcessor;
    private final LoginProcessor loginProcessor;
    private final AuthCookieService authCookieService;

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
    public ResponseEntity<VerificationResponse> startCustomerLogin(@Valid @RequestBody CustomerLoginStartRequest req) {
        return ResponseEntity.ok(authProcessor.startCustomerLogin(req));
    }

    @Operation(summary = "Register customer", description = "Creates a new customer account and issues a verification challenge")
    @PostMapping("/customer/register")
    public ResponseEntity<VerificationResponse> registerCustomer(@Valid @RequestBody CustomerRegisterRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authProcessor.registerCustomer(req));
    }

    @Operation(summary = "Start business login", description = "Issues a verification challenge for an existing business account")
    @PostMapping("/business/login/start")
    public ResponseEntity<VerificationResponse> startBusinessLogin(@Valid @RequestBody BusinessLoginStartRequest req) {
        return ResponseEntity.ok(authProcessor.startBusinessLogin(req));
    }

    @Operation(summary = "Register business", description = "Creates a new business account and issues a verification challenge")
    @PostMapping("/business/register")
    public ResponseEntity<VerificationResponse> registerBusiness(@Valid @RequestBody BusinessRegisterRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authProcessor.registerBusiness(req));
    }

    @Operation(summary = "Verify challenge code", description = "Confirms a login or registration challenge and returns an authenticated session")
    @PostMapping("/verify")
    public ResponseEntity<AuthSessionResponse> verifyCode(@Valid @RequestBody VerifyCodeRequest req) {
        return ResponseEntity.ok(authProcessor.verifyCode(req));
    }

    @Operation(summary = "Cancel verification", description = "Cancels a pending verification challenge so the user can restart with corrected credentials")
    @PostMapping("/cancel-verification")
    public ResponseEntity<Void> cancelVerification(@Valid @RequestBody CancelVerificationRequest req) {
        authProcessor.cancelVerification(req);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get current session", description = "Returns the authenticated session for the current principal")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/session")
    public ResponseEntity<AuthSessionResponse> currentSession(
            @AuthenticationPrincipal AskPrincipal principal,
            HttpServletResponse response) {
        AuthSessionResponse session = authProcessor.currentSession(principal);
        authCookieService.clear(response);
        return ResponseEntity.ok(session);
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

    @PostMapping("/email-change/request")
    public ResponseEntity<VerificationResponse> requestEmailChange(
            @AuthenticationPrincipal AskPrincipal principal,
            @Valid @RequestBody RequestEmailChangeRequest req) {
        return ResponseEntity.ok(authProcessor.requestEmailChange(principal, req));
    }

    @PostMapping("/email-change/confirm")
    public ResponseEntity<AuthSessionResponse> confirmEmailChange(
            @AuthenticationPrincipal AskPrincipal principal,
            @Valid @RequestBody VerifyCodeRequest req) {
        return ResponseEntity.ok(authProcessor.confirmEmailChange(principal, req));
    }

    @Operation(summary = "Request password change", description = "Validates the current password and sends a verification code")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/password-change/request")
    public ResponseEntity<VerificationResponse> requestPasswordChange(
            @AuthenticationPrincipal AskPrincipal principal,
            @Valid @RequestBody ChangePasswordRequest req) {
        return ResponseEntity.ok(authProcessor.requestPasswordChange(principal, req));
    }

    @Operation(summary = "Confirm password change", description = "Consumes the email challenge and changes the password")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/password-change/confirm")
    public ResponseEntity<AuthSessionResponse> confirmPasswordChange(
            @AuthenticationPrincipal AskPrincipal principal,
            @Valid @RequestBody VerifyCodeRequest req) {
        return ResponseEntity.ok(authProcessor.confirmPasswordChange(principal, req));
    }

    @Operation(summary = "Request two-factor change", description = "Sends a code before enabling or disabling two-factor authentication")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/two-factor/request")
    public ResponseEntity<VerificationResponse> requestTwoFactorChange(
            @AuthenticationPrincipal AskPrincipal principal,
            @Valid @RequestBody TwoFactorChangeRequest req) {
        return ResponseEntity.ok(authProcessor.requestTwoFactorChange(principal, req));
    }

    @Operation(summary = "Confirm two-factor change", description = "Consumes the email challenge and applies the requested state")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/two-factor/confirm")
    public ResponseEntity<AuthSessionResponse> confirmTwoFactorChange(
            @AuthenticationPrincipal AskPrincipal principal,
            @Valid @RequestBody VerifyCodeRequest req) {
        return ResponseEntity.ok(authProcessor.confirmTwoFactorChange(principal, req));
    }
}

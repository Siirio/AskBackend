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
import kz.ask.identity.api.dto.VerifyCodeRequest;
import kz.ask.identity.application.AuthProcessor;
import kz.ask.identity.application.LoginProcessor;
import kz.ask.identity.infrastructure.security.AskPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Auth", description = "Customer and business authentication, registration and session endpoints")
@RequiredArgsConstructor
public class AuthController {

    private final AuthProcessor authProcessor;
    private final LoginProcessor loginProcessor;

    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    public AuthSessionResponse login(@Valid @RequestBody LoginRequest req) {
        return loginProcessor.login(req);
    }

    @PostMapping("/change-temporary-password")
    @ResponseStatus(HttpStatus.OK)
    public AuthSessionResponse changeTemporaryPassword(@AuthenticationPrincipal AskPrincipal principal,
                                                        @Valid @RequestBody ChangeTemporaryPasswordRequest req) {
        return loginProcessor.changeTemporaryPassword(principal, req);
    }

    @Operation(summary = "Start customer login", description = "Issues a verification challenge for an existing customer account")
    @PostMapping("/customer/login/start")
    @ResponseStatus(HttpStatus.OK)
    public AuthChallengeResponse startCustomerLogin(@Valid @RequestBody CustomerLoginStartRequest req) {
        return authProcessor.startCustomerLogin(req);
    }

    @Operation(summary = "Register customer", description = "Creates a new customer account and issues a verification challenge")
    @PostMapping("/customer/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthChallengeResponse registerCustomer(@Valid @RequestBody CustomerRegisterRequest req) {
        return authProcessor.registerCustomer(req);
    }

    @Operation(summary = "Start business login", description = "Issues a verification challenge for an existing business account")
    @PostMapping("/business/login/start")
    @ResponseStatus(HttpStatus.OK)
    public AuthChallengeResponse startBusinessLogin(@Valid @RequestBody BusinessLoginStartRequest req) {
        return authProcessor.startBusinessLogin(req);
    }

    @Operation(summary = "Register business", description = "Creates a new business account and issues a verification challenge")
    @PostMapping("/business/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthChallengeResponse registerBusiness(@Valid @RequestBody BusinessRegisterRequest req) {
        return authProcessor.registerBusiness(req);
    }

    @Operation(summary = "Verify challenge code", description = "Confirms a login or registration challenge and returns an authenticated session")
    @PostMapping("/verify")
    @ResponseStatus(HttpStatus.OK)
    public AuthSessionResponse verifyCode(@Valid @RequestBody VerifyCodeRequest req) {
        return authProcessor.verifyCode(req);
    }

    @Operation(summary = "Get current session", description = "Returns the authenticated session for the current principal")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/session")
    public AuthSessionResponse currentSession(@AuthenticationPrincipal AskPrincipal principal) {
        return authProcessor.currentSession(principal);
    }

    @Operation(summary = "Logout", description = "Invalidates the current authenticated session")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/logout")
    public LogoutResponse logout(@AuthenticationPrincipal AskPrincipal principal) {
        return authProcessor.logout(principal);
    }
}

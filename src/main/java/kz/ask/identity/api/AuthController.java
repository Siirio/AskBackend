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
import kz.ask.shared.api.dto.EntityResponse;
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
    public EntityResponse<AuthSessionResponse> login(@Valid @RequestBody LoginRequest req) {
        return EntityResponse.<AuthSessionResponse>builder()
            .data(loginProcessor.login(req))
            .build();
    }

    @PostMapping("/change-temporary-password")
    @ResponseStatus(HttpStatus.OK)
    public EntityResponse<AuthSessionResponse> changeTemporaryPassword(@AuthenticationPrincipal AskPrincipal principal,
                                                        @Valid @RequestBody ChangeTemporaryPasswordRequest req) {
        return EntityResponse.<AuthSessionResponse>builder()
            .data(loginProcessor.changeTemporaryPassword(principal, req))
            .build();
    }

    @Operation(summary = "Start customer login", description = "Issues a verification challenge for an existing customer account")
    @PostMapping("/customer/login/start")
    @ResponseStatus(HttpStatus.OK)
    public EntityResponse<AuthChallengeResponse> startCustomerLogin(@Valid @RequestBody CustomerLoginStartRequest req) {
        return EntityResponse.<AuthChallengeResponse>builder()
            .data(authProcessor.startCustomerLogin(req))
            .build();
    }

    @Operation(summary = "Register customer", description = "Creates a new customer account and issues a verification challenge")
    @PostMapping("/customer/register")
    @ResponseStatus(HttpStatus.CREATED)
    public EntityResponse<AuthChallengeResponse> registerCustomer(@Valid @RequestBody CustomerRegisterRequest req) {
        return EntityResponse.<AuthChallengeResponse>builder()
            .data(authProcessor.registerCustomer(req))
            .build();
    }

    @Operation(summary = "Start business login", description = "Issues a verification challenge for an existing business account")
    @PostMapping("/business/login/start")
    @ResponseStatus(HttpStatus.OK)
    public EntityResponse<AuthChallengeResponse> startBusinessLogin(@Valid @RequestBody BusinessLoginStartRequest req) {
        return EntityResponse.<AuthChallengeResponse>builder()
            .data(authProcessor.startBusinessLogin(req))
            .build();
    }

    @Operation(summary = "Register business", description = "Creates a new business account and issues a verification challenge")
    @PostMapping("/business/register")
    @ResponseStatus(HttpStatus.CREATED)
    public EntityResponse<AuthChallengeResponse> registerBusiness(@Valid @RequestBody BusinessRegisterRequest req) {
        return EntityResponse.<AuthChallengeResponse>builder()
            .data(authProcessor.registerBusiness(req))
            .build();
    }

    @Operation(summary = "Verify challenge code", description = "Confirms a login or registration challenge and returns an authenticated session")
    @PostMapping("/verify")
    @ResponseStatus(HttpStatus.OK)
    public EntityResponse<AuthSessionResponse> verifyCode(@Valid @RequestBody VerifyCodeRequest req) {
        return EntityResponse.<AuthSessionResponse>builder()
            .data(authProcessor.verifyCode(req))
            .build();
    }

    @Operation(summary = "Get current session", description = "Returns the authenticated session for the current principal")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/session")
    public EntityResponse<AuthSessionResponse> currentSession(@AuthenticationPrincipal AskPrincipal principal) {
        return EntityResponse.<AuthSessionResponse>builder()
            .data(authProcessor.currentSession(principal))
            .build();
    }

    @Operation(summary = "Logout", description = "Invalidates the current authenticated session")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/logout")
    public EntityResponse<LogoutResponse> logout(@AuthenticationPrincipal AskPrincipal principal) {
        return EntityResponse.<LogoutResponse>builder()
            .data(authProcessor.logout(principal))
            .build();
    }
}

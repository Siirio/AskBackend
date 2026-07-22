package kz.ask.business.onboarding.api;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import kz.ask.business.onboarding.api.dto.SellerOnboardingRequest;
import kz.ask.business.onboarding.api.dto.SellerOnboardingResponse;
import kz.ask.business.onboarding.application.SellerOnboardingProcessor;
import kz.ask.identity.infrastructure.security.AskPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/business")
@RequiredArgsConstructor
public class SellerOnboardingController {

    private final SellerOnboardingProcessor sellerOnboardingProcessor;

    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/onboarding")
    public ResponseEntity<SellerOnboardingResponse> onboard(
            @AuthenticationPrincipal AskPrincipal principal,
            @Valid @RequestBody SellerOnboardingRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(sellerOnboardingProcessor.onboard(principal, request));
    }
}

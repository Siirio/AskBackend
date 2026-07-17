package kz.ask.business.api;

import jakarta.validation.Valid;
import kz.ask.business.api.dto.CompleteSellerOnboardingRequest;
import kz.ask.business.api.dto.SellerOnboardingResponse;
import kz.ask.business.application.SellerOnboardingProcessor;
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
@RequestMapping("/api/v1/seller/onboarding")
@RequiredArgsConstructor
public class SellerOnboardingController {

    private final SellerOnboardingProcessor sellerOnboardingProcessor;

    @PostMapping
    public ResponseEntity<SellerOnboardingResponse> complete(
            @AuthenticationPrincipal AskPrincipal principal,
            @Valid @RequestBody CompleteSellerOnboardingRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(sellerOnboardingProcessor.complete(principal, request));
    }
}

package kz.ask.search.api;

import jakarta.validation.Valid;
import kz.ask.identity.infrastructure.security.AskPrincipal;
import kz.ask.search.api.dto.RequestAiEnrichmentRequest;
import kz.ask.search.api.dto.RequestAiEnrichmentResponse;
import kz.ask.search.application.PlatformAiEnrichmentProcessor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/platform/ai-enrichment")
@RequiredArgsConstructor
public class PlatformAiEnrichmentController {

    private final PlatformAiEnrichmentProcessor processor;

    @PostMapping
    public ResponseEntity<RequestAiEnrichmentResponse> request(
            @AuthenticationPrincipal AskPrincipal principal,
            @Valid @RequestBody RequestAiEnrichmentRequest request) {
        return ResponseEntity.ok(processor.request(principal, request));
    }
}

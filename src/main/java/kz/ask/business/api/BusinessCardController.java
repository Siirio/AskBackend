package kz.ask.business.api;

import java.util.UUID;
import kz.ask.business.api.dto.CardResponse;
import kz.ask.business.api.dto.SaveCardRequest;
import kz.ask.business.application.BusinessCardProcessor;
import kz.ask.identity.infrastructure.security.AskPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/businesses/{businessId}/business-card")
@RequiredArgsConstructor
public class BusinessCardController {

    private final BusinessCardProcessor businessCardProcessor;

    @GetMapping
    public ResponseEntity<CardResponse> getCard(
            @AuthenticationPrincipal AskPrincipal principal,
            @PathVariable UUID businessId) {
        return ResponseEntity.ok(businessCardProcessor.getCard(principal, businessId));
    }

    @PutMapping("/draft")
    public ResponseEntity<CardResponse> saveDraft(
            @AuthenticationPrincipal AskPrincipal principal,
            @PathVariable UUID businessId,
            @RequestBody SaveCardRequest req) {
        return ResponseEntity.ok(businessCardProcessor.saveDraft(principal, businessId, req));
    }

    @PostMapping("/publish")
    public ResponseEntity<CardResponse> publish(
            @AuthenticationPrincipal AskPrincipal principal,
            @PathVariable UUID businessId) {
        return ResponseEntity.ok(businessCardProcessor.publish(principal, businessId));
    }
}

package kz.ask.business.api;

import jakarta.validation.Valid;
import java.util.UUID;
import kz.ask.business.api.dto.ShippingResponse;
import kz.ask.business.api.dto.UpdateShippingRequest;
import kz.ask.business.application.ShippingProcessor;
import kz.ask.identity.infrastructure.security.AskPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/businesses/{businessId}/shipping")
@RequiredArgsConstructor
public class ShippingController {

    private final ShippingProcessor shippingProcessor;

    @GetMapping
    public ResponseEntity<ShippingResponse> get(@AuthenticationPrincipal AskPrincipal principal,
                                                 @PathVariable UUID businessId) {
        return ResponseEntity.ok(shippingProcessor.getShipping(principal, businessId));
    }

    @PatchMapping
    public ResponseEntity<ShippingResponse> update(@AuthenticationPrincipal AskPrincipal principal,
                                                    @PathVariable UUID businessId,
                                                    @Valid @RequestBody UpdateShippingRequest req) {
        return ResponseEntity.ok(shippingProcessor.updateShipping(principal, businessId, req));
    }
}

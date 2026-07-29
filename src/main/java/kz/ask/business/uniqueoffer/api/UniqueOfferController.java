package kz.ask.business.uniqueoffer.api;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import kz.ask.business.uniqueoffer.api.dto.UniqueOfferRequest;
import kz.ask.business.uniqueoffer.api.dto.UniqueOfferResponse;
import kz.ask.business.uniqueoffer.application.UniqueOfferProcessor;
import kz.ask.business.media.application.BusinessMediaProcessor;
import kz.ask.identity.infrastructure.security.AskPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
public class UniqueOfferController {

    private final UniqueOfferProcessor uniqueOfferProcessor;
    private final BusinessMediaProcessor businessMediaProcessor;

    @GetMapping("/api/v1/businesses/{businessId}/drops")
    public ResponseEntity<List<UniqueOfferResponse>> list(@PathVariable UUID businessId) {
        return ResponseEntity.ok(uniqueOfferProcessor.list(businessId));
    }

    @PostMapping("/api/v1/businesses/{businessId}/drops")
    public ResponseEntity<UniqueOfferResponse> create(@AuthenticationPrincipal AskPrincipal principal,
                                                       @PathVariable UUID businessId,
                                                       @Valid @RequestBody UniqueOfferRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(uniqueOfferProcessor.create(principal, businessId, req));
    }

    @PatchMapping("/api/v1/drops/{dropId}")
    public ResponseEntity<UniqueOfferResponse> update(@AuthenticationPrincipal AskPrincipal principal,
                                                       @PathVariable UUID dropId,
                                                       @Valid @RequestBody UniqueOfferRequest req) {
        return ResponseEntity.ok(uniqueOfferProcessor.update(principal, dropId, req));
    }

    @PostMapping("/api/v1/drops/{dropId}/cover")
    public ResponseEntity<UniqueOfferResponse> uploadCover(
            @AuthenticationPrincipal AskPrincipal principal,
            @PathVariable UUID dropId,
            @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(businessMediaProcessor.uploadOfferCover(principal, dropId, file));
    }

    @PostMapping("/api/v1/drops/{dropId}/cancel")
    public ResponseEntity<Void> cancel(@AuthenticationPrincipal AskPrincipal principal,
                                        @PathVariable UUID dropId) {
        uniqueOfferProcessor.toggle(principal, dropId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/api/v1/drops/{dropId}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal AskPrincipal principal,
                                        @PathVariable UUID dropId) {
        businessMediaProcessor.deleteOffer(principal, dropId);
        return ResponseEntity.noContent().build();
    }
}

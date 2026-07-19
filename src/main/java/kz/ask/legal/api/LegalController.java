package kz.ask.legal.api;

import jakarta.validation.Valid;
import java.util.List;
import kz.ask.identity.infrastructure.security.AskPrincipal;
import kz.ask.legal.api.dto.AcceptLegalDocumentsRequest;
import kz.ask.legal.domain.LegalService;
import kz.ask.legal.domain.dto.LegalDocumentDto;
import kz.ask.legal.domain.enums.LegalAcceptanceChannel;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/legal")
@RequiredArgsConstructor
public class LegalController {

    private final LegalService legalService;

    @GetMapping("/documents")
    public ResponseEntity<List<LegalDocumentDto>> activeDocuments(
            @RequestParam(defaultValue = "KZ") String countryCode,
            @RequestParam(defaultValue = "ru") String locale) {
        return ResponseEntity.ok(legalService.activeDocuments(countryCode, locale));
    }

    @PostMapping("/acceptances")
    public ResponseEntity<Void> accept(
            @AuthenticationPrincipal AskPrincipal principal,
            @Valid @RequestBody AcceptLegalDocumentsRequest request) {
        return accept(principal, request, LegalAcceptanceChannel.ACCOUNT_SETTINGS);
    }

    @PostMapping("/registration-acceptances")
    public ResponseEntity<Void> acceptRegistration(
            @AuthenticationPrincipal AskPrincipal principal,
            @Valid @RequestBody AcceptLegalDocumentsRequest request) {
        return accept(principal, request, LegalAcceptanceChannel.WEB_REGISTRATION);
    }

    private ResponseEntity<Void> accept(
            AskPrincipal principal,
            AcceptLegalDocumentsRequest request,
            LegalAcceptanceChannel channel) {
        legalService.acceptActiveDocuments(
                principal.getUserId(),
                request.getDocumentCodes(),
                request.getCountryCode(),
                request.getLocale(),
                channel);
        return ResponseEntity.noContent().build();
    }
}

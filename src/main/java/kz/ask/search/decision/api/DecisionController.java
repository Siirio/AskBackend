package kz.ask.search.decision.api;

import jakarta.validation.Valid;
import kz.ask.identity.infrastructure.security.AskPrincipal;
import kz.ask.search.decision.api.dto.ClarificationRequest;
import kz.ask.search.decision.api.dto.ClarificationResponse;
import kz.ask.search.decision.api.dto.CompareRequest;
import kz.ask.search.decision.api.dto.CompareResponse;
import kz.ask.search.decision.application.processor.CompareProcessor;
import kz.ask.search.decision.infrastructure.DeepSeekClarificationStructurer;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/search")
@RequiredArgsConstructor
public class DecisionController {

    private final DeepSeekClarificationStructurer clarificationStructurer;
    private final CompareProcessor compareProcessor;

    @PostMapping("/clarification")
    public ResponseEntity<ClarificationResponse> clarify(
            @AuthenticationPrincipal AskPrincipal principal,
            @Valid @RequestBody ClarificationRequest request) {
        String category = request.getExplicitFilters() != null
                ? request.getExplicitFilters().getCategory()
                : null;
        String city = request.getExplicitFilters() != null
                ? request.getExplicitFilters().getCity()
                : null;
        return ResponseEntity.ok(clarificationStructurer.clarify(
                request.getRawQuery(),
                request.getMode().name(),
                category,
                city,
                request.getLocale()));
    }

    @PostMapping("/compare")
    public ResponseEntity<CompareResponse> compare(
            @AuthenticationPrincipal AskPrincipal principal,
            @Valid @RequestBody CompareRequest request) {
        return ResponseEntity.ok(compareProcessor.compare(
                request.getMode(),
                request.getResultIds(),
                request.getLocale()));
    }
}

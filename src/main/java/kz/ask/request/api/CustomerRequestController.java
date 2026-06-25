package kz.ask.request.api;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import kz.ask.identity.infrastructure.security.AskPrincipal;
import kz.ask.request.api.dto.CreateCustomerRequestRequest;
import kz.ask.request.api.dto.CustomerRequestDetailResponse;
import kz.ask.request.api.dto.CustomerRequestHistoryItem;
import kz.ask.request.api.dto.CustomerRequestResponse;
import kz.ask.request.application.processor.CustomerRequestProcessor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/customer-requests")
@RequiredArgsConstructor
public class CustomerRequestController {

    private final CustomerRequestProcessor processor;

    @PostMapping
    public ResponseEntity<CustomerRequestResponse> createRequest(@AuthenticationPrincipal AskPrincipal principal,
                                                                    @Valid @RequestBody CreateCustomerRequestRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(processor.createRequest(principal, req));
    }

    @GetMapping
    public ResponseEntity<List<CustomerRequestHistoryItem>> getHistory(@AuthenticationPrincipal AskPrincipal principal) {
        return ResponseEntity.ok(processor.getHistory(principal));
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<CustomerRequestDetailResponse> getDetail(@AuthenticationPrincipal AskPrincipal principal,
                                                                     @PathVariable UUID requestId) {
        return ResponseEntity.ok(processor.getDetail(principal, requestId));
    }
}

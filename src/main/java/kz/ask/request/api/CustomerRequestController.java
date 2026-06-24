package kz.ask.request.api;

import jakarta.validation.Valid;
import kz.ask.identity.infrastructure.security.AskPrincipal;
import kz.ask.request.api.dto.CreateCustomerRequestRequest;
import kz.ask.request.api.dto.CustomerRequestResponse;
import kz.ask.request.application.processor.CustomerRequestProcessor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
}

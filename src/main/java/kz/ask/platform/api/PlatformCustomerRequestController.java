package kz.ask.platform.api;

import java.util.List;
import kz.ask.identity.infrastructure.security.AskPrincipal;
import kz.ask.request.api.dto.CustomerRequestHistoryItem;
import kz.ask.request.application.processor.CustomerRequestProcessor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/platform/customer-requests")
@RequiredArgsConstructor
public class PlatformCustomerRequestController {

    private final CustomerRequestProcessor customerRequestProcessor;

    @GetMapping
    public ResponseEntity<List<CustomerRequestHistoryItem>> listAll(
            @AuthenticationPrincipal AskPrincipal principal) {
        return ResponseEntity.ok(customerRequestProcessor.listAll());
    }
}

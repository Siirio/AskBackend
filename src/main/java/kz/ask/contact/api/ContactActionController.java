package kz.ask.contact.api;

import kz.ask.contact.api.dto.ContactResolveResponse;
import kz.ask.contact.domain.ContactActionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/contacts")
@RequiredArgsConstructor
public class ContactActionController {

    private final ContactActionService contactActionService;

    @PostMapping("/{contactActionId}/resolve")
    public ResponseEntity<ContactResolveResponse> resolve(@PathVariable String contactActionId) {
        return ResponseEntity.ok(contactActionService.resolve(contactActionId));
    }
}

package kz.ask.business.api;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import kz.ask.business.api.dto.UniqueOfferRequest;
import kz.ask.business.domain.BusinessService;
import kz.ask.business.domain.UniqueOfferService;
import kz.ask.business.domain.dto.UniqueOfferDto;
import kz.ask.identity.infrastructure.security.AskPrincipal;
import kz.ask.shared.error.ForbiddenException;
import kz.ask.shared.error.ErrorCode;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/businesses/{businessId}/drops")
@RequiredArgsConstructor
public class UniqueOfferController {

    private final UniqueOfferService uniqueOfferService;
    private final BusinessService businessService;

    @GetMapping
    public ResponseEntity<List<UniqueOfferDto>> list(@PathVariable UUID businessId) {
        return ResponseEntity.ok(uniqueOfferService.listPublic(businessId));
    }

    @PostMapping
    public ResponseEntity<UniqueOfferDto> create(@AuthenticationPrincipal AskPrincipal principal,
                                                  @PathVariable UUID businessId,
                                                  @Valid @RequestBody UniqueOfferRequest req) {
        requireOwner(businessId, principal);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(uniqueOfferService.create(businessId, req.getName(), req.getDescription(),
                        req.getStartDate(), req.getEndDate(), req.getType(), req.getStatus(),
                        req.getCoverUrl(), req.getTags()));
    }

    @PatchMapping("/{dropId}")
    public ResponseEntity<UniqueOfferDto> update(@AuthenticationPrincipal AskPrincipal principal,
                                                  @PathVariable UUID businessId,
                                                  @PathVariable UUID dropId,
                                                  @Valid @RequestBody UniqueOfferRequest req) {
        requireOwner(businessId, principal);
        return ResponseEntity.ok(uniqueOfferService.update(businessId, dropId,
                req.getName(), req.getDescription(), req.getStartDate(), req.getEndDate(),
                req.getType(), req.getStatus(), req.getCoverUrl(), req.getTags()));
    }

    @PostMapping("/{dropId}/cancel")
    public ResponseEntity<UniqueOfferDto> cancel(@AuthenticationPrincipal AskPrincipal principal,
                                                  @PathVariable UUID businessId,
                                                  @PathVariable UUID dropId) {
        requireOwner(businessId, principal);
        uniqueOfferService.toggle(businessId, dropId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{dropId}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal AskPrincipal principal,
                                        @PathVariable UUID businessId,
                                        @PathVariable UUID dropId) {
        requireOwner(businessId, principal);
        uniqueOfferService.delete(businessId, dropId);
        return ResponseEntity.noContent().build();
    }

    private void requireOwner(UUID businessId, AskPrincipal principal) {
        if (!businessService.isOwnerOfBusiness(businessId, principal.getUserId())) {
            throw new ForbiddenException(ErrorCode.ACCESS_DENIED);
        }
    }
}

package kz.ask.business.uniqueoffer.api;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import kz.ask.business.uniqueoffer.api.dto.UniqueOfferRequest;
import kz.ask.business.uniqueoffer.api.dto.UniqueOfferResponse;
import kz.ask.business.core.domain.BusinessService;
import kz.ask.business.uniqueoffer.domain.UniqueOfferService;
import kz.ask.business.uniqueoffer.domain.dto.UniqueOfferDto;
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
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class UniqueOfferController {

    private final UniqueOfferService uniqueOfferService;
    private final BusinessService businessService;

    @GetMapping("/api/v1/businesses/{businessId}/drops")
    public ResponseEntity<List<UniqueOfferDto>> list(@PathVariable UUID businessId) {
        return ResponseEntity.ok(uniqueOfferService.listPublic(businessId));
    }

    @PostMapping("/api/v1/businesses/{businessId}/drops")
    public ResponseEntity<UniqueOfferResponse> create(@AuthenticationPrincipal AskPrincipal principal,
                                                       @PathVariable UUID businessId,
                                                       @Valid @RequestBody UniqueOfferRequest req) {
        requireOwner(businessId, principal);
        UniqueOfferDto dto = uniqueOfferService.create(businessId, req.getName(), req.getDescription(),
                req.getStartDate(), req.getEndDate(), req.getType(), req.getStatus(),
                req.getCoverUrl(), req.getDiscountPercent(), req.getDiscountAmount(),
                req.getIsActive(), req.getCurrency(), req.getTags());
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(dto));
    }

    @PatchMapping("/api/v1/drops/{dropId}")
    public ResponseEntity<UniqueOfferResponse> update(@AuthenticationPrincipal AskPrincipal principal,
                                                       @PathVariable UUID dropId,
                                                       @Valid @RequestBody UniqueOfferRequest req) {
        UniqueOfferDto dto = uniqueOfferService.update(dropId,
                req.getName(), req.getDescription(), req.getStartDate(), req.getEndDate(),
                req.getType(), req.getStatus(), req.getCoverUrl(), req.getDiscountPercent(),
                req.getDiscountAmount(), req.getIsActive(), req.getCurrency(), req.getTags());
        requireOwner(dto.getBusinessId(), principal);
        return ResponseEntity.ok(toResponse(dto));
    }

    @PostMapping("/api/v1/drops/{dropId}/cancel")
    public ResponseEntity<Void> cancel(@AuthenticationPrincipal AskPrincipal principal,
                                        @PathVariable UUID dropId) {
        UniqueOfferDto dto = uniqueOfferService.toggle(dropId);
        requireOwner(dto.getBusinessId(), principal);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/api/v1/drops/{dropId}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal AskPrincipal principal,
                                        @PathVariable UUID dropId) {
        UniqueOfferDto dto = uniqueOfferService.delete(dropId);
        requireOwner(dto.getBusinessId(), principal);
        return ResponseEntity.noContent().build();
    }

    private void requireOwner(UUID businessId, AskPrincipal principal) {
        if (!businessService.isOwnerOfBusiness(businessId, principal.getUserId())) {
            throw new ForbiddenException(ErrorCode.ACCESS_DENIED);
        }
    }

    private UniqueOfferResponse toResponse(UniqueOfferDto dto) {
        return UniqueOfferResponse.builder()
                .id(dto.getId())
                .businessId(dto.getBusinessId())
                .name(dto.getName())
                .description(dto.getDescription())
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .type(dto.getType())
                .status(dto.getStatus())
                .coverUrl(dto.getCoverUrl())
                .discountPercent(dto.getDiscountPercent())
                .discountAmount(dto.getDiscountAmount())
                .isActive(dto.getIsActive())
                .currency(dto.getCurrency())
                .tags(dto.getTags())
                .productIds(dto.getProductIds())
                .serviceIds(dto.getServiceIds())
                .branchIds(dto.getBranchIds())
                .build();
    }
}

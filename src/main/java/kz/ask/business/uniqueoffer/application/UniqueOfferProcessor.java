package kz.ask.business.uniqueoffer.application;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.UUID;
import kz.ask.business.branch.domain.BusinessBranchService;
import kz.ask.business.core.domain.BusinessService;
import kz.ask.business.uniqueoffer.api.dto.UniqueOfferRequest;
import kz.ask.business.uniqueoffer.api.dto.UniqueOfferResponse;
import kz.ask.business.uniqueoffer.domain.UniqueOfferService;
import kz.ask.business.uniqueoffer.domain.dto.UniqueOfferDto;
import kz.ask.identity.infrastructure.security.AskPrincipal;
import kz.ask.offer.item.domain.ItemService;
import kz.ask.offer.service.domain.ServiceService;
import kz.ask.shared.error.ErrorCode;
import kz.ask.shared.error.ForbiddenException;
import kz.ask.shared.error.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UniqueOfferProcessor {

    private final BusinessService businessService;
    private final BusinessBranchService businessBranchService;
    private final ItemService itemService;
    private final ServiceService serviceService;
    private final UniqueOfferService uniqueOfferService;

    public List<UniqueOfferResponse> list(UUID businessId) {
        return uniqueOfferService.listPublic(businessId).stream()
                .map(this::toResponse)
                .toList();
    }

    public UniqueOfferResponse create(
            AskPrincipal principal,
            UUID businessId,
            UniqueOfferRequest request) {
        requireOwner(principal, businessId);
        validateCreate(request);
        UniqueOfferDto input = toDto(request, true);
        validateRelations(businessId, input);
        return toResponse(uniqueOfferService.create(businessId, input));
    }

    public UniqueOfferResponse update(
            AskPrincipal principal,
            UUID offerId,
            UniqueOfferRequest request) {
        UniqueOfferDto current = uniqueOfferService.findById(offerId);
        requireOwner(principal, current.getBusinessId());
        UniqueOfferDto input = toDto(request, false);
        validateRelations(current.getBusinessId(), input);
        return toResponse(uniqueOfferService.update(offerId, input));
    }

    public void toggle(AskPrincipal principal, UUID offerId) {
        UniqueOfferDto current = uniqueOfferService.findById(offerId);
        requireOwner(principal, current.getBusinessId());
        uniqueOfferService.toggle(offerId);
    }

    private void requireOwner(AskPrincipal principal, UUID businessId) {
        if (!businessService.isOwnerOfBusiness(businessId, principal.getUserId())) {
            throw new ForbiddenException(ErrorCode.ACCESS_DENIED);
        }
    }

    private void validateCreate(UniqueOfferRequest request) {
        if (request.getName() == null || request.getName().isBlank()
                || request.getType() == null || request.getStatus() == null) {
            throw new ValidationException(ErrorCode.DROP_DATA_INVALID);
        }
    }

    private void validateRelations(UUID businessId, UniqueOfferDto dto) {
        if ((dto.getItemIds() != null
                && !itemService.allBelongToBusiness(businessId, dto.getItemIds()))
                || (dto.getServiceIds() != null
                && !serviceService.allBelongToBusiness(businessId, dto.getServiceIds()))
                || (dto.getBranchIds() != null
                && !businessBranchService.allBelongToBusiness(businessId, dto.getBranchIds()))) {
            throw new ValidationException(ErrorCode.DROP_RELATION_INVALID);
        }
    }

    private UniqueOfferDto toDto(UniqueOfferRequest request, Boolean create) {
        return UniqueOfferDto.builder()
                .name(request.getName())
                .description(request.getDescription())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .type(request.getType() == null ? null : request.getType().name())
                .status(request.getStatus() == null ? null : request.getStatus().name())
                .discountPercent(request.getDiscountPercent())
                .discountAmount(request.getDiscountAmount())
                .isActive(request.getIsActive())
                .currency(request.getCurrency())
                .tags(normalize(request.getTags(), create))
                .itemIds(normalize(request.getItemIds(), create))
                .serviceIds(normalize(request.getServiceIds(), create))
                .branchIds(normalize(request.getBranchIds(), create))
                .build();
    }

    private <T> List<T> normalize(List<T> values, Boolean create) {
        if (values == null) {
            return Boolean.TRUE.equals(create) ? List.of() : null;
        }
        return List.copyOf(new LinkedHashSet<>(values));
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
                .itemIds(dto.getItemIds())
                .serviceIds(dto.getServiceIds())
                .branchIds(dto.getBranchIds())
                .build();
    }
}

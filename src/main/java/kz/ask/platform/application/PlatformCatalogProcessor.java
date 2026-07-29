package kz.ask.platform.application;

import kz.ask.business.uniqueoffer.domain.entity.UniqueOffer;
import kz.ask.business.uniqueoffer.infrastructure.repository.UniqueOfferRepository;
import kz.ask.identity.infrastructure.security.AskPrincipal;
import kz.ask.offer.item.domain.entity.Item;
import kz.ask.offer.item.infrastructure.repository.ProductRepository;
import kz.ask.offer.service.domain.entity.Service;
import kz.ask.offer.service.infrastructure.repository.ServiceOfferingRepository;
import kz.ask.platform.api.dto.PlatformCatalogEntryResponse;
import kz.ask.platform.api.dto.PlatformCatalogPageResponse;
import kz.ask.platform.domain.PlatformMembershipService;
import kz.ask.shared.error.ErrorCode;
import kz.ask.shared.error.ForbiddenException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class PlatformCatalogProcessor {

    private static final int MAX_PAGE_SIZE = 100;
    private static final String CREATED_AT_FIELD = "createdAt";

    private final PlatformMembershipService platformMembershipService;
    private final ProductRepository productRepository;
    private final ServiceOfferingRepository serviceOfferingRepository;
    private final UniqueOfferRepository uniqueOfferRepository;

    @Transactional(readOnly = true)
    public PlatformCatalogPageResponse listItems(AskPrincipal principal, int page, int size) {
        requirePlatformAccess(principal);
        Page<Item> result = productRepository.findAll(pageRequest(page, size));
        return response(result.map(this::toItem));
    }

    @Transactional(readOnly = true)
    public PlatformCatalogPageResponse listServices(AskPrincipal principal, int page, int size) {
        requirePlatformAccess(principal);
        Page<Service> result = serviceOfferingRepository.findAll(pageRequest(page, size));
        return response(result.map(this::toService));
    }

    @Transactional(readOnly = true)
    public PlatformCatalogPageResponse listDrops(AskPrincipal principal, int page, int size) {
        requirePlatformAccess(principal);
        Page<UniqueOffer> result = uniqueOfferRepository.findAll(pageRequest(page, size));
        return response(result.map(this::toDrop));
    }

    private PlatformCatalogEntryResponse toItem(Item item) {
        return PlatformCatalogEntryResponse.builder()
                .id(item.getId())
                .type("ITEM")
                .name(item.getName())
                .businessId(item.getBusiness().getId())
                .businessName(item.getBusiness().getName())
                .categoryLabel(item.getCategoryLabel())
                .price(item.getPrice())
                .status(item.getModerationStatus().name())
                .isActive(item.getIsActive())
                .createdAt(item.getCreatedAt())
                .build();
    }

    private PlatformCatalogEntryResponse toService(Service service) {
        return PlatformCatalogEntryResponse.builder()
                .id(service.getId())
                .type("SERVICE")
                .name(service.getName())
                .businessId(service.getBusiness().getId())
                .businessName(service.getBusiness().getName())
                .categoryLabel(service.getCategoryLabel())
                .price(service.getBasePrice())
                .status(Boolean.TRUE.equals(service.getIsActive()) ? "ACTIVE" : "BLOCKED")
                .isActive(service.getIsActive())
                .createdAt(service.getCreatedAt())
                .build();
    }

    private PlatformCatalogEntryResponse toDrop(UniqueOffer offer) {
        return PlatformCatalogEntryResponse.builder()
                .id(offer.getId())
                .type("DROP")
                .name(offer.getName())
                .businessId(offer.getBusiness().getId())
                .businessName(offer.getBusiness().getName())
                .price(offer.getDiscountAmount())
                .status(offer.getStatus().name())
                .isActive(offer.getIsActive())
                .discountPercent(offer.getDiscountPercent())
                .discountAmount(offer.getDiscountAmount())
                .startsAt(offer.getStartDate())
                .endsAt(offer.getEndDate())
                .createdAt(offer.getCreatedAt())
                .build();
    }

    private PlatformCatalogPageResponse response(Page<PlatformCatalogEntryResponse> result) {
        return PlatformCatalogPageResponse.builder()
                .items(result.getContent())
                .page(result.getNumber())
                .size(result.getSize())
                .totalElements(result.getTotalElements())
                .totalPages(result.getTotalPages())
                .build();
    }

    private PageRequest pageRequest(int page, int size) {
        return PageRequest.of(
                Math.max(page, 0),
                Math.min(Math.max(size, 1), MAX_PAGE_SIZE),
                Sort.by(Sort.Direction.DESC, CREATED_AT_FIELD));
    }

    private void requirePlatformAccess(AskPrincipal principal) {
        if (platformMembershipService.findActiveByUser(principal.getUserId()) == null) {
            throw new ForbiddenException(ErrorCode.ACCESS_DENIED);
        }
    }
}

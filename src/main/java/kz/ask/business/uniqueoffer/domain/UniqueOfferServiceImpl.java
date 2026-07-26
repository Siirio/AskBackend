package kz.ask.business.uniqueoffer.domain;

import java.time.Instant;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import kz.ask.business.uniqueoffer.domain.dto.UniqueOfferDto;
import kz.ask.business.uniqueoffer.domain.dto.UniqueOfferBoostDto;
import kz.ask.business.uniqueoffer.domain.entity.UniqueOffer;
import kz.ask.business.uniqueoffer.domain.enums.UniqueOfferStatus;
import kz.ask.business.core.infrastructure.mapper.BusinessMapper;
import kz.ask.business.core.infrastructure.repository.BusinessRepository;
import kz.ask.business.uniqueoffer.infrastructure.repository.UniqueOfferRepository;
import kz.ask.shared.error.ErrorCode;
import kz.ask.shared.error.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UniqueOfferServiceImpl implements UniqueOfferService {

    private final UniqueOfferRepository uniqueOfferRepository;
    private final BusinessRepository businessRepository;
    private final BusinessMapper businessMapper;

    @Override
    @Transactional(readOnly = true)
    public List<UniqueOfferDto> listPublic(UUID businessId) {
        return uniqueOfferRepository.findByBusinessIdAndStatusInOrderByStartDateDesc(
                        businessId, List.of(UniqueOfferStatus.ACTIVE, UniqueOfferStatus.UPCOMING))
                .stream()
                .map(businessMapper::toUniqueOfferDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<UniqueOfferDto> listOwner(UUID businessId) {
        return uniqueOfferRepository.findByBusinessIdOrderByStartDateDesc(businessId)
                .stream()
                .map(businessMapper::toUniqueOfferDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public UniqueOfferDto findById(UUID offerId) {
        return businessMapper.toUniqueOfferDto(requireOffer(offerId));
    }

    @Override
    @Transactional
    public UniqueOfferDto create(UUID businessId, UniqueOfferDto dto) {
        UniqueOffer offer = businessMapper.toUniqueOfferEntity(
                businessRepository.getReferenceById(businessId),
                dto);
        return businessMapper.toUniqueOfferDto(uniqueOfferRepository.save(offer));
    }

    @Override
    @Transactional
    public UniqueOfferDto update(UUID offerId, UniqueOfferDto dto) {
        UniqueOffer offer = requireOffer(offerId);
        businessMapper.updateUniqueOffer(offer, dto);
        return businessMapper.toUniqueOfferDto(uniqueOfferRepository.save(offer));
    }

    @Override
    @Transactional
    public UniqueOfferDto toggle(UUID offerId) {
        UniqueOffer offer = requireOffer(offerId);
        offer.setIsActive(!offer.getIsActive());
        return businessMapper.toUniqueOfferDto(uniqueOfferRepository.save(offer));
    }

    @Override
    @Transactional
    public UniqueOfferDto delete(UUID offerId) {
        UniqueOffer offer = requireOffer(offerId);
        UniqueOfferDto dto = businessMapper.toUniqueOfferDto(offer);
        uniqueOfferRepository.delete(offer);
        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<UUID, UniqueOfferBoostDto> findActiveItemBoosts(List<UUID> itemIds) {
        if (itemIds.isEmpty()) {
            return Map.of();
        }
        List<UniqueOffer> offers = uniqueOfferRepository.findActiveLinkedItems(
                itemIds, UniqueOfferStatus.ACTIVE, Instant.now());
        return toBoosts(offers, true);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<UUID, UniqueOfferBoostDto> findActiveServiceBoosts(List<UUID> serviceIds) {
        if (serviceIds.isEmpty()) {
            return Map.of();
        }
        List<UniqueOffer> offers = uniqueOfferRepository.findActiveLinkedServices(
                serviceIds, UniqueOfferStatus.ACTIVE, Instant.now());
        return toBoosts(offers, false);
    }

    private Map<UUID, UniqueOfferBoostDto> toBoosts(List<UniqueOffer> offers, Boolean items) {
        Map<UUID, UniqueOfferBoostDto> boosts = new LinkedHashMap<>();
        offers.stream()
                .sorted(Comparator.comparing(
                        UniqueOffer::getStartDate,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .forEach(offer -> {
                    List<UUID> aggregateIds = Boolean.TRUE.equals(items)
                            ? offer.getItemIds()
                            : offer.getServiceIds();
                    List<UUID> branchIds = List.copyOf(offer.getBranchIds());
                    aggregateIds.forEach(aggregateId -> boosts.putIfAbsent(
                            aggregateId,
                            UniqueOfferBoostDto.builder()
                                    .aggregateId(aggregateId)
                                    .branchIds(branchIds)
                                    .label(offerLabel(offer))
                                    .build()));
                });
        return boosts;
    }

    private String offerLabel(UniqueOffer offer) {
        if (offer.getDiscountPercent() != null) {
            return "-" + offer.getDiscountPercent() + "%";
        }
        if (offer.getDiscountAmount() != null) {
            String currency = offer.getCurrency() == null ? "" : " " + offer.getCurrency();
            return "-" + offer.getDiscountAmount().stripTrailingZeros().toPlainString() + currency;
        }
        return offer.getName();
    }

    private UniqueOffer requireOffer(UUID offerId) {
        return uniqueOfferRepository.findById(offerId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.DROP_NOT_FOUND));
    }
}

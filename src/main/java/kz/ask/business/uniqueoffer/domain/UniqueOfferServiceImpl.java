package kz.ask.business.uniqueoffer.domain;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.math.BigDecimal;
import kz.ask.business.uniqueoffer.domain.dto.UniqueOfferDto;
import kz.ask.business.uniqueoffer.domain.entity.UniqueOffer;
import kz.ask.business.uniqueoffer.domain.enums.UniqueOfferStatus;
import kz.ask.business.uniqueoffer.domain.enums.UniqueOfferType;
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
    @Transactional
    public UniqueOfferDto create(UUID businessId, String name, String description, Instant startDate,
                                 Instant endDate, String type, String status, String coverUrl,
                                 Integer discountPercent, BigDecimal discountAmount,
                                 Boolean isActive, String currency, List<String> tags) {
        UniqueOffer offer = businessMapper.toUniqueOfferEntity(
                businessRepository.getReferenceById(businessId),
                name, description, startDate, endDate,
                UniqueOfferType.valueOf(type),
                UniqueOfferStatus.valueOf(status),
                coverUrl, discountPercent, discountAmount,
                isActive, currency, tags);
        return businessMapper.toUniqueOfferDto(uniqueOfferRepository.save(offer));
    }

    @Override
    @Transactional
    public UniqueOfferDto update(UUID offerId, String name, String description,
                                 Instant startDate, Instant endDate, String type, String status,
                                 String coverUrl, Integer discountPercent, BigDecimal discountAmount,
                                 Boolean isActive, String currency, List<String> tags) {
        UniqueOffer offer = requireOffer(offerId);
        businessMapper.updateUniqueOffer(offer, name, description, startDate, endDate,
                type, status, coverUrl, discountPercent, discountAmount,
                isActive, currency, tags);
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

    private UniqueOffer requireOffer(UUID offerId) {
        return uniqueOfferRepository.findById(offerId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.DROP_NOT_FOUND));
    }
}

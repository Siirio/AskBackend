package kz.ask.business.uniqueoffer.domain;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
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
                                 List<String> tags) {
        UniqueOffer offer = businessMapper.toUniqueOfferEntity(
                businessRepository.getReferenceById(businessId),
                name, description, startDate, endDate,
                UniqueOfferType.valueOf(type),
                UniqueOfferStatus.valueOf(status),
                coverUrl, tags);
        return businessMapper.toUniqueOfferDto(uniqueOfferRepository.save(offer));
    }

    @Override
    @Transactional
    public UniqueOfferDto update(UUID businessId, UUID offerId, String name, String description,
                                 Instant startDate, Instant endDate, String type, String status,
                                 String coverUrl, List<String> tags) {
        UniqueOffer offer = requireOffer(businessId, offerId);
        if (name != null) offer.setName(name);
        if (description != null) offer.setDescription(description);
        if (startDate != null) offer.setStartDate(startDate);
        if (endDate != null) offer.setEndDate(endDate);
        if (type != null) offer.setType(UniqueOfferType.valueOf(type));
        if (status != null) offer.setStatus(UniqueOfferStatus.valueOf(status));
        if (coverUrl != null) offer.setCoverUrl(coverUrl);
        if (tags != null) offer.setTags(tags);
        return businessMapper.toUniqueOfferDto(uniqueOfferRepository.save(offer));
    }

    @Override
    @Transactional
    public void toggle(UUID businessId, UUID offerId) {
        UniqueOffer offer = requireOffer(businessId, offerId);
        offer.setIsEnabled(!offer.getIsEnabled());
        uniqueOfferRepository.save(offer);
    }

    @Override
    @Transactional
    public void delete(UUID businessId, UUID offerId) {
        uniqueOfferRepository.delete(requireOffer(businessId, offerId));
    }

    private UniqueOffer requireOffer(UUID businessId, UUID offerId) {
        return uniqueOfferRepository.findByIdAndBusinessId(offerId, businessId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.DROP_NOT_FOUND));
    }
}

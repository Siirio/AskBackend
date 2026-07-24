package kz.ask.business.uniqueoffer.domain;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.math.BigDecimal;
import kz.ask.business.uniqueoffer.domain.dto.UniqueOfferDto;

public interface UniqueOfferService {
    List<UniqueOfferDto> listPublic(UUID businessId);

    List<UniqueOfferDto> listOwner(UUID businessId);

    UniqueOfferDto create(UUID businessId, String name, String description, Instant startDate,
                          Instant endDate, String type, String status, String coverUrl,
                          Integer discountPercent, BigDecimal discountAmount,
                          Boolean isActive, String currency, List<String> tags);

    UniqueOfferDto update(UUID offerId, String name, String description, Instant startDate,
                          Instant endDate, String type, String status, String coverUrl,
                          Integer discountPercent, BigDecimal discountAmount,
                          Boolean isActive, String currency, List<String> tags);

    UniqueOfferDto toggle(UUID offerId);

    UniqueOfferDto delete(UUID offerId);
}

package kz.ask.business.domain;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import kz.ask.business.domain.dto.UniqueOfferDto;

public interface UniqueOfferService {
    List<UniqueOfferDto> listPublic(UUID businessId);

    List<UniqueOfferDto> listOwner(UUID businessId);

    UniqueOfferDto create(UUID businessId, String name, String description, Instant startDate,
                          Instant endDate, String type, String status, String coverUrl,
                          List<String> tags);

    UniqueOfferDto update(UUID businessId, UUID offerId, String name, String description, Instant startDate,
                          Instant endDate, String type, String status, String coverUrl,
                          List<String> tags);

    void toggle(UUID businessId, UUID offerId);

    void delete(UUID businessId, UUID offerId);
}

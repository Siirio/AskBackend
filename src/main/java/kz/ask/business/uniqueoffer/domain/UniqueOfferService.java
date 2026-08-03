package kz.ask.business.uniqueoffer.domain;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import kz.ask.business.uniqueoffer.domain.dto.UniqueOfferDto;
import kz.ask.business.uniqueoffer.domain.dto.UniqueOfferBoostDto;

public interface UniqueOfferService {
    List<UniqueOfferDto> listPublic(UUID businessId);

    List<UniqueOfferDto> listOwner(UUID businessId);

    UniqueOfferDto findById(UUID offerId);

    UniqueOfferDto create(UUID businessId, UniqueOfferDto dto);

    UniqueOfferDto update(UUID offerId, UniqueOfferDto dto);

    UniqueOfferDto toggle(UUID offerId);

    UniqueOfferDto delete(UUID offerId);

    Map<UUID, UniqueOfferBoostDto> findActiveItemBoosts(List<UUID> itemIds);

    Map<UUID, UniqueOfferBoostDto> findActiveServiceBoosts(List<UUID> serviceIds);

    List<UUID> findAllActiveItemIds();

    List<UUID> findAllActiveServiceIds();
}

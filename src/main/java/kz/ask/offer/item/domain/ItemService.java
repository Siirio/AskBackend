package kz.ask.offer.item.domain;

import java.util.List;
import java.util.UUID;

public interface ItemService {

    Boolean allBelongToBusiness(UUID businessId, List<UUID> itemIds);
}

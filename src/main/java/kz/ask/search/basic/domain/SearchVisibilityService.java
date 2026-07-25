package kz.ask.search.basic.domain;

import java.util.UUID;
import kz.ask.search.basic.domain.enums.SearchEventType;

public interface SearchVisibilityService {

    void republishBusinessOffers(UUID businessId);

    void republishProductOffers(UUID productId, SearchEventType eventType);
}

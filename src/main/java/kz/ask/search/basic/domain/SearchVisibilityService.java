package kz.ask.search.basic.domain;

import java.util.UUID;

public interface SearchVisibilityService {

    void republishBusinessOffers(UUID businessId);

    void republishProductOffers(UUID productId);
}

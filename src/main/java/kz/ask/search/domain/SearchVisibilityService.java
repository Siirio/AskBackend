package kz.ask.search.domain;

import java.util.UUID;

public interface SearchVisibilityService {

    void republishBusinessOffers(UUID businessId);

    void republishProductOffers(UUID productId);
}

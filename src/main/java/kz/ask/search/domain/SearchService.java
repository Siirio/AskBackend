package kz.ask.search.domain;

import java.util.UUID;

public interface SearchService {

    void indexProductOffer(UUID offerId, UUID productId, UUID businessId, UUID branchId);
}

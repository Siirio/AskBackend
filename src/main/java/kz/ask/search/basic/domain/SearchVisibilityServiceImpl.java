package kz.ask.search.basic.domain;

import java.time.Instant;
import java.util.UUID;
import kz.ask.search.basic.domain.enums.SearchAggregateType;
import kz.ask.search.basic.domain.enums.SearchEventType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SearchVisibilityServiceImpl implements SearchVisibilityService {

    private final SearchOutboxService searchOutboxService;

    @Override
    @Transactional
    public void republishBusinessOffers(UUID businessId) {
        long version = Instant.now().toEpochMilli();
        searchOutboxService.republish(SearchAggregateType.PRODUCT_OFFER, businessId,
                SearchEventType.UPSERT, version);
        searchOutboxService.republish(SearchAggregateType.SERVICE_BRANCH_OFFER, businessId,
                SearchEventType.UPSERT, version);
    }

    @Override
    @Transactional
    public void republishProductOffers(UUID productId) {
        searchOutboxService.republish(SearchAggregateType.PRODUCT_OFFER, productId,
                SearchEventType.UPSERT, Instant.now().toEpochMilli());
    }
}

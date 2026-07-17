package kz.ask.search.domain;

import java.util.UUID;
import kz.ask.catalog.infrastructure.repository.ProductOfferRepository;
import kz.ask.search.domain.enums.SearchAggregateType;
import kz.ask.search.domain.enums.SearchEventType;
import kz.ask.service.infrastructure.repository.ServiceBranchOfferRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SearchVisibilityServiceImpl implements SearchVisibilityService {

    private final ProductOfferRepository productOfferRepository;
    private final ServiceBranchOfferRepository serviceBranchOfferRepository;
    private final SearchOutboxService searchOutboxService;

    @Override
    @Transactional
    public void republishBusinessOffers(UUID businessId) {
        productOfferRepository.findByBusinessId(businessId).forEach(offer ->
                searchOutboxService.republish(
                        SearchAggregateType.PRODUCT_OFFER, offer.getId(),
                        SearchEventType.UPSERT, offer.getSearchVersion()));
        serviceBranchOfferRepository.findByBusinessId(businessId).forEach(offer ->
                searchOutboxService.republish(
                        SearchAggregateType.SERVICE_BRANCH_OFFER, offer.getId(),
                        SearchEventType.UPSERT, offer.getSearchVersion()));
    }

    @Override
    @Transactional
    public void republishProductOffers(UUID productId) {
        productOfferRepository.findByProductId(productId).forEach(offer ->
                searchOutboxService.republish(
                        SearchAggregateType.PRODUCT_OFFER, offer.getId(),
                        SearchEventType.UPSERT, offer.getSearchVersion()));
    }
}

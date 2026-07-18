package kz.ask.business.infrastructure.scheduler;

import java.time.Instant;
import java.util.List;
import kz.ask.business.domain.entity.Business;
import kz.ask.business.domain.enums.CatalogStatus;
import kz.ask.business.infrastructure.repository.BusinessRepository;
import kz.ask.catalog.infrastructure.repository.ProductOfferRepository;
import kz.ask.search.domain.SearchVisibilityService;
import kz.ask.service.infrastructure.repository.ServiceBranchOfferRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class CatalogRestrictionScheduler {

    private final BusinessRepository businessRepository;
    private final ProductOfferRepository productOfferRepository;
    private final ServiceBranchOfferRepository serviceBranchOfferRepository;
    private final SearchVisibilityService searchVisibilityService;

    @Value("${business.catalog.auto-approve-product-count:5}")
    private long autoApproveProductCount;

    @Value("${business.catalog.auto-approve-service-count:2}")
    private long autoApproveServiceCount;

    @Scheduled(fixedDelayString = "${business.catalog.review-check-interval:PT5M}")
    @Transactional
    public void restrictExpiredCatalogs() {
        List<Business> expired = businessRepository
                .findByCatalogStatusAndCatalogDeadlineAtBefore(
                        CatalogStatus.IN_PROGRESS, Instant.now());
        expired.forEach(business -> {
            long products = productOfferRepository.countActiveProductsByBusinessId(business.getId());
            long services = serviceBranchOfferRepository.countActiveServicesByBusinessId(business.getId());
            if (products >= autoApproveProductCount || services >= autoApproveServiceCount) {
                business.setCatalogStatus(CatalogStatus.COMPLETED);
            } else if (products == 0 && services == 0) {
                business.setCatalogStatus(CatalogStatus.RESTRICTED);
            } else {
                business.setCatalogStatus(CatalogStatus.REVIEW_REQUIRED);
            }
            searchVisibilityService.republishBusinessOffers(business.getId());
        });
    }
}

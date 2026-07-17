package kz.ask.business.infrastructure.scheduler;

import java.time.Instant;
import java.util.List;
import kz.ask.business.domain.entity.Business;
import kz.ask.business.domain.enums.CatalogStatus;
import kz.ask.business.infrastructure.repository.BusinessRepository;
import kz.ask.search.domain.SearchVisibilityService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class CatalogRestrictionScheduler {

    private final BusinessRepository businessRepository;
    private final SearchVisibilityService searchVisibilityService;

    @Scheduled(fixedDelayString = "${business.catalog.restriction-check-interval:PT5M}")
    @Transactional
    public void restrictExpiredCatalogs() {
        List<Business> expired = businessRepository
                .findByCatalogStatusAndCatalogDeadlineAtBefore(
                        CatalogStatus.IN_PROGRESS, Instant.now());
        expired.forEach(business -> {
            business.setCatalogStatus(CatalogStatus.RESTRICTED);
            searchVisibilityService.republishBusinessOffers(business.getId());
        });
    }
}

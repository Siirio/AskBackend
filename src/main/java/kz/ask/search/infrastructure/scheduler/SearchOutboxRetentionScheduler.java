package kz.ask.search.infrastructure.scheduler;

import java.time.Duration;
import java.time.Instant;
import kz.ask.search.infrastructure.repository.SearchOutboxEventRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class SearchOutboxRetentionScheduler {

    private final SearchOutboxEventRepository searchOutboxEventRepository;
    private final Duration retention;

    public SearchOutboxRetentionScheduler(
            SearchOutboxEventRepository searchOutboxEventRepository,
            @Value("${ask.search.outbox.retention:P3D}") Duration retention) {
        this.searchOutboxEventRepository = searchOutboxEventRepository;
        this.retention = retention;
    }

    @Scheduled(fixedDelayString = "${ask.search.outbox.retention-interval:PT1H}")
    @Transactional
    public void deleteCompletedEvents() {
        searchOutboxEventRepository.deleteCompletedBefore(Instant.now().minus(retention));
    }
}

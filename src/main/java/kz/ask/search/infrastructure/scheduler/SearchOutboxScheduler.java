package kz.ask.search.infrastructure.scheduler;

import java.util.List;
import java.util.UUID;
import kz.ask.search.domain.SearchIndexDeliveryService;
import kz.ask.search.domain.SearchOutboxClaimService;
import kz.ask.search.domain.SearchProjectionService;
import kz.ask.search.domain.dto.SearchOutboxEventDto;
import kz.ask.search.domain.dto.SearchProjectionResult;
import kz.ask.search.domain.enums.SearchProjectionAction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SearchOutboxScheduler {

    private final SearchOutboxClaimService claimService;
    private final SearchProjectionService projectionService;
    private final SearchIndexDeliveryService deliveryService;

    @Value("${ask.search.outbox.batch-size:50}")
    private Integer batchSize;

    private final String workerId = UUID.randomUUID().toString();

    @Scheduled(fixedDelayString = "${ask.search.outbox.poll-interval:PT1S}")
    public void process() {
        List<SearchOutboxEventDto> events = claimService.claim(batchSize, workerId);
        events.forEach(this::process);
    }

    private void process(SearchOutboxEventDto event) {
        try {
            SearchProjectionResult projection = projectionService.apply(event);
            String outcome = projection.getAction() == SearchProjectionAction.STALE
                    ? SearchProjectionAction.STALE.name()
                    : deliveryService.deliver(event, projection);
            claimService.complete(event.getId(), event.getWorkerId(), outcome);
        } catch (RuntimeException failure) {
            log.warn("Search outbox event {} failed on attempt {}: {}",
                    event.getId(), event.getAttemptCount(), failure.getMessage());
            claimService.retry(event, failure);
        }
    }
}

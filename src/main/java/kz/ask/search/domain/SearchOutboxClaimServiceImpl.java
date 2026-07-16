package kz.ask.search.domain;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import kz.ask.search.domain.dto.SearchOutboxEventDto;
import kz.ask.search.domain.entity.SearchOutboxEvent;
import kz.ask.search.domain.enums.SearchOutboxStatus;
import kz.ask.search.infrastructure.repository.SearchOutboxEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SearchOutboxClaimServiceImpl implements SearchOutboxClaimService {

    private static final Integer ERROR_LIMIT = 2000;

    private final SearchOutboxEventRepository repository;

    @Value("${ask.search.outbox.max-attempts:8}")
    private Integer maxAttempts;

    @Value("${ask.search.outbox.retry-base-delay:PT5S}")
    private Duration retryBaseDelay;

    @Value("${ask.search.outbox.retry-max-delay:PT10M}")
    private Duration retryMaxDelay;

    @Value("${ask.search.outbox.processing-timeout:PT5M}")
    private Duration processingTimeout;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public List<SearchOutboxEventDto> claim(Integer batchSize, String workerId) {
        Instant now = Instant.now();
        repository.recoverAbandoned(now.minus(processingTimeout), now);
        repository.completeSuperseded(now);
        List<SearchOutboxEvent> events = repository.lockClaimable(now, batchSize);
        events.forEach(event -> {
            event.setStatus(SearchOutboxStatus.PROCESSING);
            event.setWorkerId(workerId);
            event.setProcessingStartedAt(now);
            event.setAttemptCount(event.getAttemptCount() + 1);
            event.setLastError(null);
        });
        return events.stream().map(this::toDto).toList();
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void retry(SearchOutboxEventDto event, RuntimeException failure) {
        Instant now = Instant.now();
        String error = truncate(failure.getMessage());
        if (event.getAttemptCount() >= maxAttempts) {
            repository.finish(event.getId(), event.getWorkerId(), SearchOutboxStatus.DEAD.name(), now, error);
            return;
        }
        long multiplier = 1L << Math.min(event.getAttemptCount() - 1, 20);
        Duration delay = retryBaseDelay.multipliedBy(multiplier);
        if (delay.compareTo(retryMaxDelay) > 0) {
            delay = retryMaxDelay;
        }
        repository.markRetry(event.getId(), event.getWorkerId(), now.plus(delay), error, now);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void complete(UUID eventId, String workerId, String outcome) {
        Instant now = Instant.now();
        repository.finish(eventId, workerId, SearchOutboxStatus.COMPLETED.name(), now, truncate(outcome));
    }

    private SearchOutboxEventDto toDto(SearchOutboxEvent event) {
        return SearchOutboxEventDto.builder()
                .id(event.getId())
                .aggregateType(event.getAggregateType())
                .aggregateId(event.getAggregateId())
                .eventType(event.getEventType())
                .aggregateVersion(event.getAggregateVersion())
                .attemptCount(event.getAttemptCount())
                .workerId(event.getWorkerId())
                .build();
    }

    private String truncate(String value) {
        if (value == null || value.length() <= ERROR_LIMIT) {
            return value;
        }
        return value.substring(0, ERROR_LIMIT);
    }
}

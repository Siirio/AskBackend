package kz.ask.search.domain;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import kz.ask.search.domain.entity.SearchIndexQueue;
import kz.ask.search.infrastructure.repository.SearchIndexQueueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SearchIndexQueueServiceImpl implements SearchIndexQueueService {

    private static final int DEBOUNCE_MINUTES = 3;
    private static final int MAX_RETRIES = 5;
    private static final int RETRY_DELAY_MINUTES = 10;

    private final SearchIndexQueueRepository repository;

    @Override
    @Transactional
    public void schedule(String entityType, UUID entityId) {
        repository.findByEntityTypeAndEntityId(entityType, entityId)
                .ifPresentOrElse(
                        existing -> existing.setScheduledAt(Instant.now().plus(DEBOUNCE_MINUTES, ChronoUnit.MINUTES)),
                        () -> upsert(entityType, entityId, Instant.now().plus(DEBOUNCE_MINUTES, ChronoUnit.MINUTES)));
    }

    @Override
    @Transactional
    public void scheduleNow(String entityType, UUID entityId) {
        repository.findByEntityTypeAndEntityId(entityType, entityId)
                .ifPresentOrElse(
                        existing -> existing.setScheduledAt(Instant.now()),
                        () -> upsert(entityType, entityId, Instant.now()));
    }

    @Transactional
    public void markCompleted(String entityType, UUID entityId) {
        repository.deleteByEntityTypeAndEntityId(entityType, entityId);
    }

    @Transactional
    public void markFailed(String entityType, UUID entityId, String error) {
        repository.findByEntityTypeAndEntityId(entityType, entityId).ifPresent(entry -> {
            entry.setRetryCount(entry.getRetryCount() + 1);
            entry.setLastError(error);
            if (entry.getRetryCount() >= MAX_RETRIES) {
                repository.delete(entry);
            } else {
                long delay = RETRY_DELAY_MINUTES * (long) Math.pow(2, entry.getRetryCount() - 1);
                entry.setScheduledAt(Instant.now().plus(delay, ChronoUnit.MINUTES));
            }
        });
    }

    private void upsert(String entityType, UUID entityId, Instant scheduledAt) {
        SearchIndexQueue entry = new SearchIndexQueue();
        entry.setEntityType(entityType);
        entry.setEntityId(entityId);
        entry.setScheduledAt(scheduledAt);
        entry.setRetryCount(0);
        repository.save(entry);
    }
}

package kz.ask.search.domain;

import java.util.UUID;

public interface SearchIndexQueueService {

    void schedule(String entityType, UUID entityId);

    void scheduleNow(String entityType, UUID entityId);
}

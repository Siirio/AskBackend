package kz.ask.search.domain;

import java.util.UUID;
import kz.ask.search.domain.enums.SearchAggregateType;
import kz.ask.search.domain.enums.SearchEventType;

public interface SearchOutboxService {

    void publish(SearchAggregateType aggregateType, UUID aggregateId,
                 SearchEventType eventType, Long aggregateVersion);

    void republish(SearchAggregateType aggregateType, UUID aggregateId,
                   SearchEventType eventType, Long aggregateVersion);
}

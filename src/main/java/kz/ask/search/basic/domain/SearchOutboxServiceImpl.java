package kz.ask.search.basic.domain;

import java.time.Instant;
import java.util.UUID;
import kz.ask.search.basic.domain.enums.SearchAggregateType;
import kz.ask.search.basic.domain.enums.SearchEventType;
import kz.ask.search.basic.infrastructure.repository.SearchOutboxEventRepository;
import kz.ask.shared.infrastructure.uuid.UuidV7;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SearchOutboxServiceImpl implements SearchOutboxService {

    private static final Integer PAYLOAD_VERSION = 1;

    private final SearchOutboxEventRepository repository;

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void publish(SearchAggregateType aggregateType, UUID aggregateId,
                        SearchEventType eventType, Long aggregateVersion) {
        Instant now = Instant.now();
        repository.insertIfAbsent(
                UuidV7.create(), aggregateType.name(), aggregateId, eventType.name(),
                aggregateVersion, PAYLOAD_VERSION, now, now);
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void republish(SearchAggregateType aggregateType, UUID aggregateId,
                          SearchEventType eventType, Long aggregateVersion) {
        Instant now = Instant.now();
        repository.requeue(
                UuidV7.create(), aggregateType.name(), aggregateId, eventType.name(),
                aggregateVersion, PAYLOAD_VERSION, now, now);
    }
}

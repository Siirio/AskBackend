package kz.ask.search.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import kz.ask.search.domain.enums.SearchAggregateType;
import kz.ask.search.domain.enums.SearchEventType;
import kz.ask.search.domain.enums.SearchOutboxStatus;
import kz.ask.shared.domain.entity.BaseUuidV7Entity;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "search_outbox_event")
public class SearchOutboxEvent extends BaseUuidV7Entity {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SearchAggregateType aggregateType;

    @Column(nullable = false)
    private UUID aggregateId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SearchEventType eventType;

    @Column(nullable = false)
    private Long aggregateVersion;

    @Column(nullable = false)
    private Integer payloadVersion = 1;

    @Column(nullable = false)
    private Instant availableAt;

    private Instant processingStartedAt;

    private Instant processedAt;

    private String workerId;

    @Column(nullable = false)
    private Integer attemptCount = 0;

    @Column(length = 2000)
    private String lastError;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SearchOutboxStatus status = SearchOutboxStatus.PENDING;
}

package kz.ask.search.domain.dto;

import java.util.UUID;
import kz.ask.search.domain.enums.SearchAggregateType;
import kz.ask.search.domain.enums.SearchEventType;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SearchOutboxEventDto {

    private UUID id;
    private SearchAggregateType aggregateType;
    private UUID aggregateId;
    private SearchEventType eventType;
    private Long aggregateVersion;
    private Integer attemptCount;
    private String workerId;
}

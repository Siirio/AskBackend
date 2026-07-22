package kz.ask.search.domain.dto;

import java.util.UUID;
import kz.ask.search.domain.enums.SearchAggregateType;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SearchAiEnrichmentItem {

    private UUID documentId;
    private SearchAggregateType aggregateType;
    private UUID aggregateId;
    private Long updatedAtMillis;
    private String title;
    private String description;
    private String categoryLabel;
    private String aliases;
    private Integer attemptCount;
    private String workerId;
}

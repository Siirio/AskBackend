package kz.ask.search.basic.domain.dto;

import java.util.List;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SearchReindexBatch {

    private List<MeilisearchIndexDocument> documents;
    private UUID nextCursor;
    private Boolean hasMore;
}

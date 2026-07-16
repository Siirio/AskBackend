package kz.ask.search.domain.dto;

import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SearchReconciliationBatch {

    private UUID nextCursor;
    private Boolean hasMore;
    private Integer scanned;
    private Integer mismatches;
    private Integer repairsQueued;
}

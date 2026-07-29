package kz.ask.search.basic.domain.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DeadEventRepairBatch {

    private Integer scanned;
    private Integer wouldRebuild;
    private Integer wouldDelete;
    private Integer wouldSupersede;
    private Integer skipped;
    private Integer requeued;
    private Integer completed;
}

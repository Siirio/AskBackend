package kz.ask.search.basic.domain.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DeadEventRepairBatch {

    private Integer scanned;
    private Integer requeued;
    private Integer completed;
    private Integer skipped;
}

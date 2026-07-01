package kz.ask.autodump.domain.dto;

import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ImportSessionDto {

    private UUID id;
    private UUID businessId;
    private UUID branchId;
    private UUID createdBy;
    private String sourceType;
    private String status;
    private String inputSummary;
    private Integer totalDraftCount;
    private Integer approvedCount;
    private Integer rejectedCount;
    private Integer errorCount;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant completedAt;
}

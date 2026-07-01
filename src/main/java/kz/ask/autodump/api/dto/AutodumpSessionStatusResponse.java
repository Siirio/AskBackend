package kz.ask.autodump.api.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import kz.ask.autodump.domain.dto.DraftItemDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AutodumpSessionStatusResponse {

    private UUID sessionId;
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
    private List<DraftItemDto> drafts;
}

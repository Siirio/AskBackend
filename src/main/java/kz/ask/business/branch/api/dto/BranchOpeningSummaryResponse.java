package kz.ask.business.branch.api.dto;

import java.time.Instant;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BranchOpeningSummaryResponse {
    private String state;
    private String timeZoneId;
    private Instant evaluatedAt;
    private Instant nextOpensAt;
    private Instant nextClosesAt;
}

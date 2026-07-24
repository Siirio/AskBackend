package kz.ask.business.branch.domain.dto;

import java.time.Instant;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BranchOpeningSummary {

    private String state;
    private String timeZoneId;
    private Instant evaluatedAt;
    private Instant nextOpensAt;
    private Instant nextClosesAt;
}

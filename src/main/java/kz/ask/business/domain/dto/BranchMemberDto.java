package kz.ask.business.domain.dto;

import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class BranchMemberDto {

    private UUID id;
    private UUID userId;
    private String userEmail;
    private String userDisplayName;
    private String userStatus;
    private String role;
    private Instant userActivatedAt;
}

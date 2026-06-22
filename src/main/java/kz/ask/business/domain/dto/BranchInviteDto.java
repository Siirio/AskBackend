package kz.ask.business.domain.dto;

import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class BranchInviteDto {

    private UUID id;
    private String code;
    private String role;
    private Integer maxUses;
    private Integer useCount;
    private Instant expiresAt;
    private Instant revokedAt;
}

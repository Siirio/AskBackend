package kz.ask.business.api.dto;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class BusinessInvitationResponse {

    private UUID id;
    private UUID businessId;
    private String businessName;
    private String invitedEmail;
    private String invitedRole;
    private String invitedByDisplayName;
    private String status;
    private Instant expiresAt;
    private Set<UUID> branchIds;
}

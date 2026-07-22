package kz.ask.business.invitation.domain.dto;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class BusinessInvitationDto {

    private UUID id;
    private UUID businessId;
    private String businessName;
    private String invitedEmail;
    private String invitedRole;
    private UUID invitedByUserId;
    private String invitedByDisplayName;
    private String status;
    private Instant expiresAt;
    private Set<UUID> branchIds;
}

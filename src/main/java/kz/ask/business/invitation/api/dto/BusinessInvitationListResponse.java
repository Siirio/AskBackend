package kz.ask.business.invitation.api.dto;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BusinessInvitationListResponse {
    private List<BusinessInvitationResponse> invitations;
}

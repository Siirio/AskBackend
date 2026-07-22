package kz.ask.business.invitation.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.util.Set;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class CreateBusinessInvitationRequest {

    @Email
    @NotBlank
    private String invitedEmail;

    @NotBlank
    private String invitedRole;

    private Set<UUID> branchIds;
}

package kz.ask.identity.api.dto;

import java.util.List;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class AuthBusinessMembershipResponse {

    private UUID membershipId;
    private UUID businessId;
    private String businessName;
    private String role;
    private List<UUID> branchIds;
}

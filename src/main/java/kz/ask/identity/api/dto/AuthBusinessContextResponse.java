package kz.ask.identity.api.dto;

import java.util.UUID;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class AuthBusinessContextResponse {

    private UUID businessId;
    private String businessName;
    private UUID branchId;
    private String branchName;
    private UUID membershipId;
    private String memberRole;
}

package kz.ask.business.api.dto;

import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class StaffResponse {

    private UUID id;
    private String email;
    private String displayName;
    private String role;
    private String branchName;
    private String status;
    private String tempPassword;
    private Instant activatedAt;
    private String businessName;
    private UUID businessId;
}

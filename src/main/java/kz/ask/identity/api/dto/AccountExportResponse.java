package kz.ask.identity.api.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AccountExportResponse {

    private UUID userId;
    private String email;
    private String displayName;
    private String status;
    private List<BusinessMembershipExport> businessMemberships;
    private PlatformMembershipExport platformMembership;
    private List<LegalAcceptanceExport> legalAcceptances;

    @Getter
    @Builder
    public static class BusinessMembershipExport {
        private UUID businessId;
        private String businessName;
        private String role;
        private String status;
    }

    @Getter
    @Builder
    public static class PlatformMembershipExport {
        private String role;
        private String status;
        private List<String> permissions;
    }

    @Getter
    @Builder
    public static class LegalAcceptanceExport {
        private String documentCode;
        private String documentVersion;
        private String countryCode;
        private String locale;
        private String acceptanceChannel;
        private Instant acceptedAt;
    }
}

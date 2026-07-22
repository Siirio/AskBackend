package kz.ask.moderation.api.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import kz.ask.business.domain.enums.VerificationStatus;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class VerificationDetailResponse {

    private UUID businessId;
    private String businessName;
    private VerificationStatus status;
    private String binIin;
    private String twoGisUrl;
    private String kaspiUrl;
    private String ozonUrl;
    private String wildberriesUrl;
    private String websiteUrl;
    private String instagramUrl;
    private String telegramUrl;
    private String phone;
    private String corporateEmail;
    private Instant createdAt;
    private Instant updatedAt;
    private List<VerificationHistoryItem> history;

    @Getter
    @Builder
    public static class VerificationHistoryItem {
        private VerificationStatus fromStatus;
        private VerificationStatus toStatus;
        private UUID editedBy;
        private String comment;
        private Instant createdAt;
    }
}

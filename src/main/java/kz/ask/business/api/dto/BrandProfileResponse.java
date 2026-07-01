package kz.ask.business.api.dto;

import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BrandProfileResponse {
    private UUID id;
    private UUID businessId;
    private String businessName;
    private String brandColor;
    private String logoUrl;
    private String coverUrl;
    private String toneOfVoice;
    private String description;
    private String instagramUrl;
    private String telegramUrl;
    private String websiteUrl;
}

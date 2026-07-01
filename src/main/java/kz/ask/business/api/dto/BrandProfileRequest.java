package kz.ask.business.api.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BrandProfileRequest {
    private String brandColor;
    private String logoUrl;
    private String coverUrl;
    private String toneOfVoice;
    private String description;
    private String instagramUrl;
    private String telegramUrl;
    private String websiteUrl;
}

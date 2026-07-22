package kz.ask.business.api.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BusinessProfileRequest {
    private String brandColor;
    private String logoUrl;
    private String coverUrl;
    private String description;
    private String number;
    private String email;
    private String instagramUrl;
    private String telegramUrl;
    private String websiteUrl;
}

package kz.ask.search.basic.api.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SearchBusinessProfileResponse {

    private String logoUrl;
    private String coverUrl;
    private String description;
    private String number;
    private String email;
    private String instagramUrl;
    private String telegramUrl;
    private String websiteUrl;
}

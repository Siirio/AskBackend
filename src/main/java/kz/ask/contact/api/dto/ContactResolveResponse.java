package kz.ask.contact.api.dto;

import java.time.Instant;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ContactResolveResponse {
    private String actionType;
    private String redirectUrl;
    private String deepLink;
    private String displayValue;
    private String provider;
    private String label;
    private Instant expiresAt;
}

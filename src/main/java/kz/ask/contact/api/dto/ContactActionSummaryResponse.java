package kz.ask.contact.api.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ContactActionSummaryResponse {
    private String contactActionId;
    private String provider;
    private String label;
}

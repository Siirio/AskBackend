package kz.ask.business.profile.api.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;
import kz.ask.business.profile.domain.enums.DeliveryCoverage;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BusinessProfileRequest {

    private static final String HTTP_URL_PATTERN = "^(?:|https?://\\S+)$";

    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$")
    private String brandColor;

    @Size(max = 2000)
    private String description;

    @Size(max = 32)
    private String number;

    @Email
    @Size(max = 320)
    private String email;

    @Size(max = 2048)
    @Pattern(regexp = HTTP_URL_PATTERN)
    private String instagramUrl;

    @Size(max = 2048)
    @Pattern(regexp = HTTP_URL_PATTERN)
    private String telegramUrl;

    @Size(max = 2048)
    @Pattern(regexp = HTTP_URL_PATTERN)
    private String websiteUrl;

    private DeliveryCoverage deliveryCoverage;

    @Size(max = 50)
    private List<@Size(max = 120) String> deliveryCities;

    private Boolean pickupAvailable;

    @AssertTrue(message = "At least one delivery city is required for selected city coverage")
    public boolean isDeliveryCoverageValid() {
        return deliveryCoverage == null
                || deliveryCoverage != DeliveryCoverage.SELECTED_CITIES
                || deliveryCities != null && deliveryCities.stream().anyMatch(this::hasText);
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}

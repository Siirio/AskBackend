package kz.ask.offer.purchase.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PurchaseDestinationRequest {

    @NotBlank
    @Size(max = 255)
    private String label;

    @NotBlank
    @Size(max = 2048)
    @Pattern(regexp = "(?i)^https?://\\S+$")
    private String url;
}

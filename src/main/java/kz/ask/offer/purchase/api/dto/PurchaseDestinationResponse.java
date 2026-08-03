package kz.ask.offer.purchase.api.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PurchaseDestinationResponse {
    private String label;
    private String url;
}

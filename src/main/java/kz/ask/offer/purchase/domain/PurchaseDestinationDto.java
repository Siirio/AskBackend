package kz.ask.offer.purchase.domain;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PurchaseDestinationDto {
    private String label;
    private String url;
}

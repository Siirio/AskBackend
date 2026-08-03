package kz.ask.offer.purchase.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

@Embeddable
@Getter
@Setter
public class PurchaseDestination {

    @Column(name = "label", nullable = false)
    private String label;

    @Column(name = "url", nullable = false, length = 2048)
    private String url;
}

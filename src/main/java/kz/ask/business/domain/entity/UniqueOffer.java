package kz.ask.business.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import kz.ask.business.domain.enums.UniqueOfferStatus;
import kz.ask.business.domain.enums.UniqueOfferType;
import kz.ask.shared.domain.entity.BaseUuidV7Entity;
import kz.ask.shared.infrastructure.converter.JsonListConverter;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "unique_offer")
public class UniqueOffer extends BaseUuidV7Entity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "business_id", nullable = false)
    private Business business;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    private Instant startDate;

    private Instant endDate;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private UniqueOfferType type;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private UniqueOfferStatus status;

    private String coverUrl;

    @Column(name = "discount_percent")
    private Integer discountPercent;

    @Column(name = "discount_amount")
    private java.math.BigDecimal discountAmount;

    @Column(nullable = false)
    private Boolean enabled = true;

    @Column(length = 3, columnDefinition = "VARCHAR(3) DEFAULT 'KZT'")
    private String currency;

    @Convert(converter = JsonListConverter.class)
    @Column(columnDefinition = "JSONB")
    private List<String> tags = new ArrayList<>();
}

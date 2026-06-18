package kz.ask.catalog.domain.entity;

import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import kz.ask.business.domain.entity.BusinessBranch;
import kz.ask.business.domain.entity.DataSource;
import kz.ask.catalog.domain.enums.StockStatus;
import kz.ask.shared.domain.entity.BaseUuidV7Entity;
import kz.ask.shared.domain.enums.AvailabilityConfidence;
import kz.ask.shared.domain.enums.RecordStatus;

@Entity
@Getter
@Setter
@Table(name = "product_offer")
public class ProductOffer extends BaseUuidV7Entity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "branch_id", nullable = false)
    private BusinessBranch branch;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "data_source_id")
    private DataSource dataSource;

    private BigDecimal price;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private StockStatus stockStatus;

    private Integer stockQuantity;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private AvailabilityConfidence availabilityConfidence;

    private Instant freshnessAt;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private RecordStatus status;
}

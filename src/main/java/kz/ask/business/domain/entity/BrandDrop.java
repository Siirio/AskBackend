package kz.ask.business.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import kz.ask.business.domain.enums.BrandDropStatus;
import kz.ask.business.domain.enums.BrandDropType;
import kz.ask.shared.domain.entity.BaseUuidV7Entity;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "brand_drop")
public class BrandDrop extends BaseUuidV7Entity {

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
    private BrandDropType type;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private BrandDropStatus status;

    private String coverUrl;
}

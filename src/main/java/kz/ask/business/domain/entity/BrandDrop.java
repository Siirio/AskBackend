package kz.ask.business.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.ElementCollection;
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
import java.util.UUID;
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

    @ElementCollection
    @CollectionTable(name = "brand_drop_tag", joinColumns = @JoinColumn(name = "brand_drop_id"))
    @Column(name = "tag", nullable = false)
    private List<String> tags = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "brand_drop_product", joinColumns = @JoinColumn(name = "brand_drop_id"))
    @Column(name = "product_id", nullable = false)
    private List<UUID> productIds = new ArrayList<>();
}

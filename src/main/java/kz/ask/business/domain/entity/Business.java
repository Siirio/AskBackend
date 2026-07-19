package kz.ask.business.domain.entity;

import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.Column;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.JoinColumn;
import java.util.LinkedHashSet;
import java.util.Set;
import java.time.Instant;
import kz.ask.business.domain.enums.BusinessLegalForm;
import kz.ask.business.domain.enums.BusinessModerationStatus;
import kz.ask.business.domain.enums.CatalogSetupMode;
import kz.ask.business.domain.enums.CatalogScope;
import kz.ask.business.domain.enums.CatalogStatus;
import kz.ask.business.domain.enums.CatalogSourceType;
import kz.ask.business.domain.enums.PreferredContactChannel;
import kz.ask.shared.domain.entity.BaseUuidV7Entity;
import kz.ask.shared.domain.enums.RecordStatus;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Getter
@Setter
@Table(name = "business")
public class Business extends BaseUuidV7Entity {

    @Column(nullable = false)
    private String name;

    private String legalName;

    private String bin;

    private String countryCode;

    @Enumerated(EnumType.STRING)
    private BusinessLegalForm legalForm;

    private String legalIdentifier;

    @Enumerated(EnumType.STRING)
    private PreferredContactChannel preferredContactChannel;

    private String preferredContactValue;

    @Enumerated(EnumType.STRING)
    private CatalogSetupMode catalogSetupMode;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private CatalogScope catalogScope;

    @ElementCollection
    @CollectionTable(
            name = "business_catalog_source",
            joinColumns = @JoinColumn(name = "business_id"))
    @Column(name = "source_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private Set<CatalogSourceType> catalogSources = new LinkedHashSet<>();

    private String catalogSourceLinks;

    private String catalogSourceNotes;

    private Instant catalogDeadlineAt;

    @Enumerated(EnumType.STRING)
    private CatalogStatus catalogStatus;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private BusinessModerationStatus moderationStatus = BusinessModerationStatus.VISIBLE;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private RecordStatus status;

    @Column(length = 3, columnDefinition = "VARCHAR(3) DEFAULT 'KZT'")
    private String currency;

    @Column(name = "shipping_mode", length = 20)
    private String shippingMode;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "shipping_city_ids", columnDefinition = "JSONB")
    private String shippingCityIds;
}

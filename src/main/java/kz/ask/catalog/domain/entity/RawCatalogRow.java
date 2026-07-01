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
import kz.ask.catalog.domain.enums.RawRowStatus;
import kz.ask.shared.domain.entity.BaseUuidV7Entity;

@Entity
@Getter
@Setter
@Table(name = "raw_catalog_row")
public class RawCatalogRow extends BaseUuidV7Entity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "catalog_import_id", nullable = false)
    private CatalogImport catalogImport;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_offer_id")
    private ProductOffer productOffer;

    @Column(nullable = false)
    private Integer rowNumber;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String rowPayload;

    @Column(name = "normalized_data_json", columnDefinition = "TEXT")
    private String normalizedDataJson;

    @Column(name = "validation_errors_json", columnDefinition = "TEXT")
    private String validationErrorsJson;

    @Column(name = "validation_warnings_json", columnDefinition = "TEXT")
    private String validationWarningsJson;

    @Enumerated(EnumType.STRING)
    private RawRowStatus status;
}

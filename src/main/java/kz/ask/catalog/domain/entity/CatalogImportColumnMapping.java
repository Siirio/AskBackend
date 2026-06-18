package kz.ask.catalog.domain.entity;

import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import kz.ask.shared.domain.entity.BaseUuidV7Entity;

@Entity
@Getter
@Setter
@Table(name = "catalog_import_column_mapping")
public class CatalogImportColumnMapping extends BaseUuidV7Entity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "catalog_import_id", nullable = false)
    private CatalogImport catalogImport;

    @Column(nullable = false)
    private String sourceColumn;

    @Column(nullable = false)
    private String targetField;
}

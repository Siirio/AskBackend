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
import java.time.Instant;
import kz.ask.business.domain.entity.Business;
import kz.ask.business.domain.entity.BusinessBranch;
import kz.ask.business.domain.entity.DataSource;
import kz.ask.catalog.domain.enums.CatalogImportStatus;
import kz.ask.identity.domain.entity.AppUser;
import kz.ask.shared.domain.entity.BaseUuidV7Entity;

@Entity
@Getter
@Setter
@Table(name = "catalog_import")
public class CatalogImport extends BaseUuidV7Entity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "data_source_id", nullable = false)
    private DataSource dataSource;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_id")
    private Business business;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id")
    private BusinessBranch branch;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private AppUser createdBy;

    @Column(nullable = false)
    private String originalFileName;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private CatalogImportStatus status;

    @Column(name = "total_rows")
    private Integer totalRows;

    @Column(name = "valid_rows")
    private Integer validRows;

    @Column(name = "invalid_rows")
    private Integer invalidRows;

    @Column(name = "warning_rows")
    private Integer warningRows;

    private Instant importedAt;
}

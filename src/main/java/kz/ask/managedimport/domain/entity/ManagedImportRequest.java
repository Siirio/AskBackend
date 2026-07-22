package kz.ask.managedimport.domain.entity;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import kz.ask.business.domain.entity.Business;
import kz.ask.business.domain.enums.CatalogSourceType;
import kz.ask.business.domain.enums.CatalogScope;
import kz.ask.business.domain.enums.PreferredContactChannel;
import kz.ask.identity.domain.entity.AppUser;
import kz.ask.managedimport.domain.enums.ManagedImportStatus;
import kz.ask.shared.domain.entity.BaseUuidV7Entity;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "managed_import_request")
public class ManagedImportRequest extends BaseUuidV7Entity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "business_id", nullable = false)
    private Business business;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "requested_by_user_id", nullable = false)
    private AppUser requestedBy;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ManagedImportStatus status;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private CatalogScope catalogScope;

    @ElementCollection
    @CollectionTable(
            name = "managed_import_request_source",
            joinColumns = @JoinColumn(name = "managed_import_request_id"))
    @Column(name = "source_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private Set<CatalogSourceType> selectedSourceTypes;

    private String sourceLinks; 

    private String sourceNotes;

    private Instant expiresAt;

    private Instant completedAt;
}

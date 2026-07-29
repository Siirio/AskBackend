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
import kz.ask.business.core.domain.entity.Business;
import kz.ask.business.core.domain.enums.ImportSourceType;
import kz.ask.business.core.domain.enums.BusinessScope;
import kz.ask.business.core.domain.enums.PreferredContactChannel;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responsible_platform_user_id")
    private AppUser responsiblePlatformUser;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ManagedImportStatus status;

    @Column(name = "business_scope", nullable = false)
    @Enumerated(EnumType.STRING)
    private BusinessScope businessScope;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private PreferredContactChannel preferredContactChannel;

    @Column(nullable = false)
    private String preferredContactValue;

    private UUID conversationId;

    @ElementCollection
    @CollectionTable(
            name = "managed_import_request_source",
            joinColumns = @JoinColumn(name = "managed_import_request_id"))
    @Column(name = "source_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private Set<ImportSourceType> selectedSourceTypes;

    private String sourceLinks; 

    private String sourceNotes;

    private Instant expiresAt;

    private Instant activatedAt;

    private Instant completedAt;

    private Integer productsPublishedCount;
}

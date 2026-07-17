package kz.ask.managedimport.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import kz.ask.business.domain.entity.Business;
import kz.ask.identity.domain.entity.AppUser;
import kz.ask.managedimport.domain.enums.ManagedImportGrantStatus;
import kz.ask.shared.domain.entity.BaseUuidV7Entity;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "managed_import_grant")
public class ManagedImportGrant extends BaseUuidV7Entity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "business_id", nullable = false)
    private Business business;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "managed_import_request_id", nullable = false)
    private ManagedImportRequest managedImportRequest;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "granted_by_user_id", nullable = false)
    private AppUser grantedBy;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ManagedImportGrantStatus status;

    @Column(nullable = false)
    private Instant grantedAt;

    private Instant revokedAt;
}

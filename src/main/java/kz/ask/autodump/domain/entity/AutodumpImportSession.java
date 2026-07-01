package kz.ask.autodump.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import kz.ask.autodump.domain.enums.ImportSessionStatus;
import kz.ask.autodump.domain.enums.SourceType;
import kz.ask.business.domain.entity.Business;
import kz.ask.business.domain.entity.BusinessBranch;
import kz.ask.identity.domain.entity.AppUser;
import kz.ask.shared.domain.entity.BaseUuidV7Entity;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "autodump_import_session")
public class AutodumpImportSession extends BaseUuidV7Entity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "business_id", nullable = false)
    private Business business;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "branch_id", nullable = false)
    private BusinessBranch branch;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by", nullable = false)
    private AppUser createdBy;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private SourceType sourceType;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ImportSessionStatus status;

    private String inputSummary;

    private Integer totalDraftCount;

    private Integer approvedCount;

    private Integer rejectedCount;

    private Integer errorCount;

    private Instant completedAt;
}

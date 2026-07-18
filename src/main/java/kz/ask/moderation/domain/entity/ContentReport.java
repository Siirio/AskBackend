package kz.ask.moderation.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import kz.ask.identity.domain.entity.AppUser;
import kz.ask.moderation.domain.enums.ContentReportStatus;
import kz.ask.moderation.domain.enums.ReportTargetType;
import kz.ask.shared.domain.entity.BaseUuidV7Entity;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "content_report")
public class ContentReport extends BaseUuidV7Entity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reporter_user_id", nullable = false)
    private AppUser reporter;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ReportTargetType targetType;

    @Column(nullable = false)
    private UUID targetId;

    @Column(nullable = false)
    private String reasonCode;

    private String details;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ContentReportStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resolved_by_user_id")
    private AppUser resolvedBy;

    private String resolution;

    private Instant resolvedAt;
}

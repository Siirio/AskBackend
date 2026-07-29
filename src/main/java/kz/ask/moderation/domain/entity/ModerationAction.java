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
import kz.ask.platform.domain.enums.ModerationActionType;
import kz.ask.platform.domain.enums.ModerationStatus;
import kz.ask.platform.domain.enums.ModerationTargetType;
import kz.ask.shared.domain.entity.BaseUuidV7Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "moderation_action")
public class ModerationAction extends BaseUuidV7Entity {

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ModerationTargetType targetType;

    @Column(nullable = false)
    private UUID targetId;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ModerationActionType action;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ModerationStatus moderationStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "performed_by_user_id", nullable = false)
    private AppUser performedBy;

    @Column(name = "expires_at")
    private Instant expiresAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "made_by_user_id")
    private AppUser madeBy;

    @Column(name = "reason_code")
    private String reasonCode;

    private String details;

    @Column(length = 2000)
    private String note;
}

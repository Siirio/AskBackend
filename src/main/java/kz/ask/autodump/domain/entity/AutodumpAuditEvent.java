package kz.ask.autodump.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import kz.ask.autodump.domain.enums.AuditEventType;
import kz.ask.identity.domain.entity.AppUser;
import kz.ask.shared.domain.entity.BaseUuidV7Entity;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "autodump_audit_event")
public class AutodumpAuditEvent extends BaseUuidV7Entity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "import_session_id", nullable = false)
    private AutodumpImportSession importSession;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "draft_item_id")
    private AutodumpDraftItem draftItem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_user_id")
    private AppUser actorUser;

    @Column(nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private AuditEventType eventType;

    @Column(columnDefinition = "TEXT")
    private String payloadJson;
}

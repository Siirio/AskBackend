package kz.ask.autodump.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import kz.ask.autodump.domain.enums.ErrorSeverity;
import kz.ask.shared.domain.entity.BaseUuidV7Entity;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "autodump_import_error")
public class AutodumpImportError extends BaseUuidV7Entity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "import_session_id", nullable = false)
    private AutodumpImportSession importSession;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "draft_item_id")
    private AutodumpDraftItem draftItem;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private ErrorSeverity severity;

    @Column(nullable = false, length = 100)
    private String code;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(columnDefinition = "TEXT")
    private String payloadJson;
}

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
import kz.ask.autodump.domain.enums.AiJobStatus;
import kz.ask.shared.domain.entity.BaseUuidV7Entity;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "autodump_ai_job")
public class AutodumpAiJob extends BaseUuidV7Entity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "import_session_id", nullable = false)
    private AutodumpImportSession importSession;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "raw_input_id")
    private AutodumpRawInput rawInput;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private AiJobStatus status;

    private String provider;

    private String model;

    private String promptVersion;

    private Integer inputTokenEstimate;

    private Integer outputTokenEstimate;

    @Column(columnDefinition = "TEXT")
    private String rawResponseJson;

    private String errorMessage;

    private Integer attemptCount;

    private Instant startedAt;

    private Instant finishedAt;
}

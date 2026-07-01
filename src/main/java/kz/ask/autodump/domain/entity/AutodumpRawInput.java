package kz.ask.autodump.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import kz.ask.autodump.domain.enums.StorageKind;
import kz.ask.shared.domain.entity.BaseUuidV7Entity;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "autodump_raw_input")
public class AutodumpRawInput extends BaseUuidV7Entity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "import_session_id", nullable = false)
    private AutodumpImportSession importSession;

    private String originalFileName;

    private String contentType;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private StorageKind storageKind;

    private String storageRef;

    @Column(columnDefinition = "TEXT")
    private String rawText;

    @Column(length = 64)
    private String sha256;

    private Long sizeBytes;
}

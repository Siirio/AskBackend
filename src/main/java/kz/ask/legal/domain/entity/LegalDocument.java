package kz.ask.legal.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.Instant;
import kz.ask.legal.domain.enums.LegalDocumentCode;
import kz.ask.shared.domain.entity.BaseUuidV7Entity;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "legal_document")
public class LegalDocument extends BaseUuidV7Entity {

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private LegalDocumentCode code;

    @Column(nullable = false)
    private String version;

    @Column(nullable = false)
    private String countryCode;

    @Column(nullable = false)
    private String publicUrl;

    @Column(nullable = false)
    private Instant effectiveAt;

    @Column(nullable = false)
    private Boolean isActive;
}

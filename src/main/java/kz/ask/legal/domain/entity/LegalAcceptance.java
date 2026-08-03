package kz.ask.legal.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import kz.ask.identity.domain.entity.AppUser;
import kz.ask.legal.domain.enums.LegalAcceptanceChannel;
import kz.ask.legal.domain.enums.LegalDocumentCode;
import kz.ask.shared.domain.entity.BaseUuidV7Entity;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "legal_acceptance")
public class LegalAcceptance extends BaseUuidV7Entity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private LegalDocumentCode documentCode;

    @Column(nullable = false)
    private String documentVersion;

    @Column(nullable = false)
    private String countryCode;

    @Column(nullable = false)
    private String locale;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private LegalAcceptanceChannel acceptanceChannel;

    @Column(nullable = false)
    private Instant acceptedAt;
}

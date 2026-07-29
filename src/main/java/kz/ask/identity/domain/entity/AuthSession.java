package kz.ask.identity.domain.entity;

import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.time.Instant;
import kz.ask.shared.domain.entity.BaseUuidV7Entity;

@Entity
@Getter
@Setter
@Table(name = "auth_session")
public class AuthSession extends BaseUuidV7Entity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @Column(nullable = false, unique = true)
    private String tokenHash;

    @Column(nullable = false)
    private String authority;

    @Column(nullable = false)
    private Boolean isRemembered;

    @Column(nullable = false)
    private Boolean isActivationRequired;

    @Column(nullable = false)
    private Instant expiresAt;

    private Instant revokedAt;

    @Transient
    private String plainToken;
}

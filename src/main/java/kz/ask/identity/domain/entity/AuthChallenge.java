package kz.ask.identity.domain.entity;

import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.time.Instant;
import kz.ask.identity.domain.enums.AuthChallengeChannel;
import kz.ask.identity.domain.enums.AuthChallengePurpose;
import kz.ask.identity.domain.enums.AuthChallengeStatus;
import kz.ask.shared.domain.entity.BaseUuidV7Entity;

@Entity
@Getter
@Setter
@Table(name = "auth_challenge")
public class AuthChallenge extends BaseUuidV7Entity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private AppUser user;

    private String email;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private AuthChallengeChannel channel;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private AuthChallengePurpose purpose;

    @Column(nullable = false)
    private String codeHash;

    @Column(nullable = false)
    private Integer attempts;

    @Column(nullable = false)
    private Integer maxAttempts;

    @Column(nullable = false)
    private Instant expiresAt;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private AuthChallengeStatus status;

    private Boolean rememberMe;

    private String registrationData;

    @Transient
    private String codePlain;
}

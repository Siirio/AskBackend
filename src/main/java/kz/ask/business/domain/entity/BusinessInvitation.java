package kz.ask.business.domain.entity;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import kz.ask.business.domain.enums.BusinessInvitationStatus;
import kz.ask.business.domain.enums.BusinessMemberRole;
import kz.ask.identity.domain.entity.AppUser;
import kz.ask.shared.domain.entity.BaseUuidV7Entity;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "business_invitation")
public class BusinessInvitation extends BaseUuidV7Entity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "business_id", nullable = false)
    private Business business;

    @Column(nullable = false)
    private String invitedEmail;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private BusinessMemberRole invitedRole;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "invited_by_user_id", nullable = false)
    private AppUser invitedBy;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private BusinessInvitationStatus status;

    @Column(nullable = false)
    private String tokenHash;

    @Column(nullable = false)
    private Instant expiresAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "accepted_by_user_id")
    private AppUser acceptedBy; //status of acceptance cannot be part of the invitation, we need a separate table for tracking the acceptance/declining. Actually Make one ENUM that says the status - accepted, declined, revoked or suspended by platform user(ModerationStatus) and it must have app user, creation and last updated time(base entity extension)

    private Instant acceptedAt;

    private Instant declinedAt;

    private Instant revokedAt;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "business_invitation_branch",
            joinColumns = @JoinColumn(name = "business_invitation_id"))
    @Column(name = "branch_id", nullable = false)
    private Set<UUID> branchIds;
}

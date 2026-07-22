package kz.ask.business.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import kz.ask.business.domain.enums.InvitationActionType;
import kz.ask.identity.domain.entity.AppUser;
import kz.ask.shared.domain.entity.BaseUuidV7Entity;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "business_invitation_action")
public class BusinessInvitationAction extends BaseUuidV7Entity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "invitation_id", nullable = false)
    private BusinessInvitation invitation;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private InvitationActionType actionType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "acted_by_user_id")
    private AppUser actedBy;
}

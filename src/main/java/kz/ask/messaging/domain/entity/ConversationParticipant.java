package kz.ask.messaging.domain.entity;

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
import kz.ask.business.domain.entity.Business;
import kz.ask.business.domain.entity.BusinessBranch;
import kz.ask.identity.domain.entity.AppUser;
import kz.ask.messaging.domain.enums.ConversationParticipantType;
import kz.ask.shared.domain.entity.BaseUuidV7Entity;

@Entity
@Getter
@Setter
@Table(name = "conversation_participant")
public class ConversationParticipant extends BaseUuidV7Entity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "conversation_id", nullable = false)
    private Conversation conversation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private AppUser user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_id")
    private Business business;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id")
    private BusinessBranch branch;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ConversationParticipantType participantType;
}

package kz.ask.service.domain.entity;

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
import java.time.Instant;
import kz.ask.business.domain.entity.BusinessBranch;
import kz.ask.identity.domain.entity.AppUser;
import kz.ask.messaging.domain.entity.Conversation;
import kz.ask.service.domain.enums.BookingSource;
import kz.ask.service.domain.enums.BookingStatus;
import kz.ask.shared.domain.entity.BaseUuidV7Entity;

@Entity
@Getter
@Setter
@Table(name = "booking")
public class Booking extends BaseUuidV7Entity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private AppUser customer;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "service_branch_offer_id", nullable = false)
    private ServiceBranchOffer serviceBranchOffer;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "branch_id", nullable = false)
    private BusinessBranch branch;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id")
    private Conversation conversation;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private BookingStatus status;

    private Instant requestedStartAt;

    private Instant confirmedStartAt;

    private Instant confirmedEndAt;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private BookingSource source;

    private String customerNote;

    private String providerNote;
}

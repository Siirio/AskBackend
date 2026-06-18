package kz.ask.messaging.domain.entity;

import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import kz.ask.catalog.domain.entity.ProductOffer;
import kz.ask.request.domain.entity.CustomerRequest;
import kz.ask.service.domain.entity.Booking;
import kz.ask.service.domain.entity.ServiceBranchOffer;
import kz.ask.shared.domain.entity.BaseUuidV7Entity;

@Entity
@Getter
@Setter
@Table(name = "conversation_link")
public class ConversationLink extends BaseUuidV7Entity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "conversation_id", nullable = false)
    private Conversation conversation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_request_id")
    private CustomerRequest customerRequest;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id")
    private Booking booking;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_offer_id")
    private ProductOffer productOffer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_branch_offer_id")
    private ServiceBranchOffer serviceBranchOffer;
}

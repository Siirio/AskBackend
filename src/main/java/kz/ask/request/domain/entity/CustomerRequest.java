package kz.ask.request.domain.entity;

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
import kz.ask.business.domain.entity.Category;
import kz.ask.business.domain.entity.City;
import kz.ask.catalog.domain.entity.ProductOffer;
import kz.ask.identity.domain.entity.AppUser;
import kz.ask.request.domain.enums.CustomerRequestStatus;
import kz.ask.service.domain.entity.ServiceBranchOffer;
import kz.ask.shared.domain.entity.BaseUuidV7Entity;

@Entity
@Getter
@Setter
@Table(name = "customer_request")
public class CustomerRequest extends BaseUuidV7Entity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "city_id", nullable = false)
    private City city;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_offer_id")
    private ProductOffer productOffer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_branch_offer_id")
    private ServiceBranchOffer serviceBranchOffer;

    @Column(nullable = false)
    private String queryText;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private CustomerRequestStatus status;

    private Instant requestedStartAt;

    private Instant expiresAt;
}

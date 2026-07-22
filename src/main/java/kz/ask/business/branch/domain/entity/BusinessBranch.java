package kz.ask.business.branch.domain.entity;

import kz.ask.business.core.domain.entity.Business;

import kz.ask.shared.domain.entity.City;
import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import kz.ask.shared.domain.entity.BaseUuidV7Entity;

@Entity
@Getter
@Setter
@Table(name = "business_branch")
public class BusinessBranch extends BaseUuidV7Entity {

    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "city_id")
    private City city;

    private String address;

    @Column(length = 512)
    private String addressDetails;

    @Column(nullable = false)
    private Boolean isOnlineOnly;

    private OffsetDateTime workingHourStart;

    private OffsetDateTime workingHourEnd;

    private BigDecimal latitude;

    private BigDecimal longitude;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "business_id", nullable = false)
    private Business business;
}

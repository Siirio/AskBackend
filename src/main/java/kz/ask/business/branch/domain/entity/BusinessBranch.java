package kz.ask.business.branch.domain.entity;

import kz.ask.business.core.domain.entity.Business;

import kz.ask.shared.domain.entity.City;
import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
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

    private BigDecimal latitude;

    private BigDecimal longitude;

    @Column(name = "time_zone_id")
    private String timeZoneId;

    @ElementCollection
    @CollectionTable(name = "branch_weekly_hours",
            joinColumns = @JoinColumn(name = "branch_id"))
    private List<WeeklyOpeningInterval> weeklyHours = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "branch_special_hours",
            joinColumns = @JoinColumn(name = "branch_id"))
    private List<SpecialOpeningInterval> specialHours = new ArrayList<>();

    @Column(name = "pickup_available")
    private Boolean pickupAvailable = false;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "business_id", nullable = false)
    private Business business;
}

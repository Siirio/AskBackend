package kz.ask.business.profile.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.util.List;
import kz.ask.business.core.domain.entity.Business;
import kz.ask.business.profile.domain.enums.DeliveryCoverage;
import kz.ask.shared.domain.entity.BaseUuidV7Entity;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "business_profile")
public class BusinessProfile extends BaseUuidV7Entity {

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "business_id", nullable = false, unique = true)
    private Business business;

    private String brandColor;

    private String logoUrl;

    private String coverUrl;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String number;

    private String email;

    private String instagramUrl;

    private String telegramUrl;

    private String websiteUrl;

    @Enumerated(EnumType.STRING)
    private DeliveryCoverage deliveryCoverage;

    @ElementCollection
    @CollectionTable(name = "business_profile_delivery_city",
            joinColumns = @JoinColumn(name = "business_profile_id"))
    @Column(name = "city_name", nullable = false)
    private List<String> deliveryCities;

    private Boolean pickupAvailable;
}

package kz.ask.business.domain.entity;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import kz.ask.business.domain.enums.DeliveryScope;
import kz.ask.shared.domain.entity.BaseUuidV7Entity;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "business_delivery_profile")
public class BusinessDeliveryProfile extends BaseUuidV7Entity {

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "business_id", nullable = false)
    private Business business;

    @Column(nullable = false)
    private Boolean pickupAvailable;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private DeliveryScope deliveryScope;

    @ElementCollection
    @CollectionTable(
            name = "business_delivery_city",
            joinColumns = @JoinColumn(name = "delivery_profile_id"))
    @Column(name = "city_id", nullable = false)
    private Set<UUID> selectedCityIds = new LinkedHashSet<>();

    private String termsRu;
    private String termsKk;
    private String termsEn;
}

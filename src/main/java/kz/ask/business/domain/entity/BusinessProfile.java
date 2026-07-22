package kz.ask.business.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
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
}

package kz.ask.business.verification.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import kz.ask.business.core.domain.entity.Business;
import kz.ask.business.verification.domain.enums.VerificationStatus;
import kz.ask.shared.domain.entity.BaseUuidV7Entity;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "business_verification")
public class BusinessVerification extends BaseUuidV7Entity {

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "business_id", nullable = false, unique = true)
    private Business business;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private VerificationStatus status;

    @Column(name = "two_gis_url")
    private String twoGisUrl;

    @Column(name = "kaspi_url")
    private String kaspiUrl;

    @Column(name = "ozon_url")
    private String ozonUrl;

    @Column(name = "wildberries_url")
    private String wildberriesUrl;

    @Column(name = "website_url")
    private String websiteUrl;

    @Column(name = "instagram_url")
    private String instagramUrl;

    @Column(name = "telegram_url")
    private String telegramUrl;

    private String phone;

    @Column(name = "corporate_email")
    private String corporateEmail;
}

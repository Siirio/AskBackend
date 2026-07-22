package kz.ask.business.domain.entity;

import jakarta.persistence.*;
import kz.ask.business.domain.enums.BusinessLegalForm;
import kz.ask.business.domain.enums.CatalogScope;
import kz.ask.business.domain.enums.CatalogSetupMode;
import kz.ask.shared.domain.entity.BaseUuidV7Entity;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "business")
public class Business extends BaseUuidV7Entity {

    @Column(nullable = false)
    private String name;

    private String legalName;

    private String bin;

    private String iin;

    private String legalIdentifier;

    private String countryCode;

    @Column(length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    private BusinessLegalForm legalForm;

    @Enumerated(EnumType.STRING)
    private CatalogSetupMode catalogSetupMode;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private CatalogScope catalogScope;

    @Column(nullable = false)
    private Boolean isOnline;
}

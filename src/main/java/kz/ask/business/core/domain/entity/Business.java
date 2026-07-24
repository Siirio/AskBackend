package kz.ask.business.core.domain.entity;

import jakarta.persistence.*;
import kz.ask.business.category.domain.entity.Category;
import kz.ask.business.core.domain.enums.BusinessLegalForm;
import kz.ask.business.core.domain.enums.BusinessScope;
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

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private BusinessScope scope;

    @Column(nullable = false)
    private Boolean onlineOnly;
}

package kz.ask.business.domain.entity;

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
import kz.ask.business.domain.enums.ContactType;
import kz.ask.shared.domain.entity.BaseUuidV7Entity;
import kz.ask.shared.domain.enums.RecordStatus;

@Entity
@Getter
@Setter
@Table(name = "business_contact")
public class BusinessContact extends BaseUuidV7Entity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "business_id", nullable = false)
    private Business business;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id")
    private BusinessBranch branch;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ContactType contactType;

    @Column(nullable = false)
    private String contactValue;

    @Column(name = "is_primary", nullable = false)
    private Boolean primaryContact;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private RecordStatus status;
}

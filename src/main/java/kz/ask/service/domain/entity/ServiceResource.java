package kz.ask.service.domain.entity;

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
import kz.ask.business.domain.entity.BusinessBranch;
import kz.ask.service.domain.enums.ResourceType;
import kz.ask.shared.domain.entity.BaseUuidV7Entity;
import kz.ask.shared.domain.enums.RecordStatus;

@Entity
@Getter
@Setter
@Table(name = "service_resource")
public class ServiceResource extends BaseUuidV7Entity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "branch_id", nullable = false)
    private BusinessBranch branch;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ResourceType resourceType;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private RecordStatus status;
}

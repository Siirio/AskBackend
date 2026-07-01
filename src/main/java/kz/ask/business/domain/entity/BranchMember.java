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
import kz.ask.business.domain.enums.BranchMemberRole;
import kz.ask.identity.domain.entity.AppUser;
import kz.ask.shared.domain.entity.BaseUuidV7Entity;
import kz.ask.shared.domain.enums.RecordStatus;

@Entity
@Getter
@Setter
@Table(name = "branch_member")
public class BranchMember extends BaseUuidV7Entity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "branch_id", nullable = false)
    private BusinessBranch branch;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private BranchMemberRole role;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private RecordStatus status;
}

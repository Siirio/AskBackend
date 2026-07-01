package kz.ask.search.domain.entity;

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
import java.math.BigDecimal;
import kz.ask.business.domain.entity.Business;
import kz.ask.business.domain.entity.BusinessBranch;
import kz.ask.business.domain.enums.DataSourceType;
import kz.ask.catalog.domain.entity.ProductOffer;
import kz.ask.search.domain.enums.SearchResultSnapshotType;
import kz.ask.service.domain.entity.ServiceBranchOffer;
import kz.ask.shared.domain.entity.BaseUuidV7Entity;

@Entity
@Getter
@Setter
@Table(name = "search_result_snapshot")
public class SearchResultSnapshot extends BaseUuidV7Entity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "search_snapshot_id", nullable = false)
    private SearchSnapshot searchSnapshot;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private SearchResultSnapshotType resultType;

    @Column(nullable = false)
    private Integer resultRank;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_offer_id")
    private ProductOffer productOffer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_branch_offer_id")
    private ServiceBranchOffer serviceBranchOffer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_id")
    private Business business;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id")
    private BusinessBranch branch;

    @Column(nullable = false)
    private String title;

    private String summary;

    private BigDecimal price;

    private String statusLabelKey;

    @Enumerated(EnumType.STRING)
    private DataSourceType sourceType;

    private Integer distanceMeters;
}

package kz.ask.search.domain.entity;

import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import kz.ask.business.domain.entity.Business;
import kz.ask.business.domain.entity.BusinessBranch;
import kz.ask.business.domain.entity.BrandDrop;
import kz.ask.catalog.domain.entity.ProductOffer;
import kz.ask.search.domain.enums.SearchDocumentType;
import kz.ask.service.domain.entity.ServiceBranchOffer;
import kz.ask.shared.domain.entity.BaseUuidV7Entity;
import kz.ask.shared.domain.enums.RecordStatus;

@Entity
@Getter
@Setter
@Table(name = "search_document")
public class SearchDocument extends BaseUuidV7Entity {

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private SearchDocumentType documentType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_offer_id")
    private ProductOffer productOffer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_branch_offer_id")
    private ServiceBranchOffer serviceBranchOffer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_drop_id")
    private BrandDrop brandDrop;

    @Column(nullable = false)
    private String title;

    private String summary;

    @Column(name = "category_label")
    private String categoryLabel;

    private String sku;

    @Column(name = "characteristics_json", columnDefinition = "TEXT")
    private String characteristicsJson;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_id")
    private Business business;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id")
    private BusinessBranch branch;

    private BigDecimal price;

    @ElementCollection
    @CollectionTable(name = "search_document_token", joinColumns = @JoinColumn(name = "search_document_id"))
    @Column(name = "token", nullable = false)
    private List<String> tokens = new ArrayList<>();

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private RecordStatus status;

    private String source;

    @Column(name = "public_note")
    private String publicNote;
}

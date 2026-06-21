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
import java.util.ArrayList;
import java.util.List;
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

    @Column(nullable = false)
    private String title;

    private String summary;

    @ElementCollection
    @CollectionTable(name = "search_document_token", joinColumns = @JoinColumn(name = "search_document_id"))
    @Column(name = "token", nullable = false)
    private List<String> tokens = new ArrayList<>();

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private RecordStatus status;
}

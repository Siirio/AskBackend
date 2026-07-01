package kz.ask.autodump.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.UUID;
import kz.ask.autodump.domain.enums.DraftItemStatus;
import kz.ask.catalog.domain.entity.ProductOffer;
import kz.ask.service.domain.entity.ServiceBranchOffer;
import kz.ask.shared.domain.entity.BaseUuidV7Entity;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "autodump_draft_item")
public class AutodumpDraftItem extends BaseUuidV7Entity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "import_session_id", nullable = false)
    private AutodumpImportSession importSession;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ai_job_id")
    private AutodumpAiJob aiJob;

    @Column(nullable = false, length = 20)
    private String itemType;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private DraftItemStatus status;

    private String title;

    private String normalizedTitle;

    private String categoryLabel;

    private String subcategoryLabel;

    @Column(columnDefinition = "TEXT")
    private String description;

    private BigDecimal price;

    private String priceText;

    @Column(length = 10)
    private String currency;

    private String brand;

    @Column(columnDefinition = "TEXT")
    private String tagsJson;

    @Column(columnDefinition = "TEXT")
    private String customAttributesJson;

    @Column(columnDefinition = "TEXT")
    private String sourceReference;

    @Column(columnDefinition = "TEXT")
    private String confidenceNotes;

    private Boolean needsReview;

    private String duplicateGroupKey;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "published_product_offer_id")
    private ProductOffer publishedProductOffer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "published_service_branch_offer_id")
    private ServiceBranchOffer publishedServiceBranchOffer;
}

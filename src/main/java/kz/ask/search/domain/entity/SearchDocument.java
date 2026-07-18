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
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import kz.ask.business.domain.entity.Business;
import kz.ask.business.domain.entity.BusinessBranch;
import kz.ask.catalog.domain.entity.ProductOffer;
import kz.ask.search.domain.enums.SearchDocumentType;
import kz.ask.search.domain.enums.SearchAvailabilitySource;
import kz.ask.search.domain.enums.SearchAvailabilityStatus;
import kz.ask.service.domain.entity.ServiceBranchOffer;
import kz.ask.shared.domain.entity.BaseUuidV7Entity;
import kz.ask.shared.domain.enums.RecordStatus;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Getter
@Setter
@Table(name = "search_document")
public class SearchDocument extends BaseUuidV7Entity {

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private SearchDocumentType documentType;

    @Column(name = "aggregate_id", nullable = false)
    private java.util.UUID aggregateId;

    @Column(name = "document_version", nullable = false)
    private Long documentVersion = 0L;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_offer_id")
    private ProductOffer productOffer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_branch_offer_id")
    private ServiceBranchOffer serviceBranchOffer;

    @Column(nullable = false)
    private String title;

    @Column(name = "normalized_title", nullable = false)
    private String normalizedTitle = "";

    private String summary;

    @Column(name = "category_label")
    private String categoryLabel;

    private String sku;

    private String brand;

    @Column(name = "category_path")
    private String categoryPath;

    @Column(name = "business_name")
    private String businessName;

    @Column(name = "branch_name")
    private String branchName;

    @Column(name = "characteristics_json", columnDefinition = "TEXT")
    private String characteristicsJson;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_id")
    private Business business;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id")
    private BusinessBranch branch;

    private BigDecimal price;

    @Column(nullable = false, length = 3)
    private String currency = "KZT";

    private BigDecimal latitude;

    private BigDecimal longitude;

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

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "verified_attributes", nullable = false, columnDefinition = "JSONB")
    private Map<String, Object> verifiedAttributes = new HashMap<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "ai_attributes", nullable = false, columnDefinition = "JSONB")
    private Map<String, Object> aiAttributes = new HashMap<>();

    @Column(nullable = false, columnDefinition = "TEXT")
    private String aliases = "";

    @Column(name = "ai_search_summary")
    private String aiSearchSummary;

    @Enumerated(EnumType.STRING)
    @Column(name = "availability_status", nullable = false)
    private SearchAvailabilityStatus availabilityStatus = SearchAvailabilityStatus.UNKNOWN;

    @Enumerated(EnumType.STRING)
    @Column(name = "availability_source", nullable = false)
    private SearchAvailabilitySource availabilitySource = SearchAvailabilitySource.UNKNOWN;

    @Column(name = "last_business_updated_at")
    private Instant lastBusinessUpdatedAt;

    @Column(name = "indexed_at")
    private Instant indexedAt;

    @Column(name = "ai_enrichment_version")
    private Long aiEnrichmentVersion;

    @Column(name = "ai_enrichment_available_at", nullable = false)
    private Instant aiEnrichmentAvailableAt = Instant.now();

    @Column(name = "ai_enrichment_started_at")
    private Instant aiEnrichmentStartedAt;

    @Column(name = "ai_enrichment_worker_id")
    private String aiEnrichmentWorkerId;

    @Column(name = "ai_enrichment_attempt_count", nullable = false)
    private Integer aiEnrichmentAttemptCount = 0;

    @Column(name = "ai_enrichment_error", length = 2000)
    private String aiEnrichmentError;

    @Column(name = "ai_enrichment_dead", nullable = false)
    private Boolean aiEnrichmentDead = Boolean.FALSE;

    @Column(name = "ai_enrichment_requested", nullable = false)
    private Boolean aiEnrichmentRequested = Boolean.FALSE;
}

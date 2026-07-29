package kz.ask.search.basic.domain.entity;

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
import java.util.List;
import java.util.Map;
import kz.ask.business.core.domain.entity.Business;
import kz.ask.business.branch.domain.entity.BusinessBranch;
import kz.ask.search.basic.domain.enums.SearchAvailabilitySource;
import kz.ask.search.basic.domain.enums.SearchAvailabilityStatus;
import kz.ask.search.basic.domain.enums.SearchDocumentType;
import kz.ask.search.basic.domain.enums.SearchProjectionAction;
import kz.ask.shared.domain.entity.BaseUuidV7Entity;
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

    @Column
    private String title;

    @Column(name = "normalized_title")
    private String normalizedTitle;

    private String summary;

    @Column(name = "category_label")
    private String categoryLabel;

    private String brand;

    @Column(name = "category_path")
    private String categoryPath;

    @Column(name = "business_name")
    private String businessName;

    @Column(name = "branch_name")
    private String branchName;

    @Column(name = "business_id")
    private java.util.UUID businessId;

    @Column(name = "branch_id")
    private java.util.UUID branchId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_id", insertable = false, updatable = false)
    private Business business;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id", insertable = false, updatable = false)
    private BusinessBranch branch;

    private BigDecimal price;

    @Column(length = 3)
    private String currency;

    private BigDecimal latitude;

    private BigDecimal longitude;

    @ElementCollection
    @CollectionTable(name = "search_document_token", joinColumns = @JoinColumn(name = "search_document_id"))
    @Column(name = "token", nullable = false)
    private List<String> tokens;

    private String source;

    @Column(name = "public_note")
    private String publicNote;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "verified_attributes", columnDefinition = "JSONB")
    private Map<String, Object> verifiedAttributes;

    @Column(name = "embedding_text", columnDefinition = "TEXT")
    private String embeddingText;

    @Enumerated(EnumType.STRING)
    @Column(name = "availability_status")
    private SearchAvailabilityStatus availabilityStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "availability_source")
    private SearchAvailabilitySource availabilitySource;

    @Column(name = "last_business_updated_at")
    private Instant lastBusinessUpdatedAt;

    @Column(name = "indexed_at")
    private Instant indexedAt;

    @Column(name = "projection_version", nullable = false)
    private Long projectionVersion;

    @Column(name = "indexed_version")
    private Long indexedVersion;

    @Enumerated(EnumType.STRING)
    @Column(name = "projection_action", nullable = false)
    private SearchProjectionAction projectionAction;

}

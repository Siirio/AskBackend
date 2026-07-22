package kz.ask.item.domain.entity;

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
import java.util.List;
import java.util.Map;
import kz.ask.business.domain.entity.Business;
import kz.ask.business.domain.entity.BusinessBranch;
import kz.ask.item.domain.enums.ProductModerationStatus;
import kz.ask.shared.domain.entity.BaseUuidV7Entity;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Getter
@Setter
@Table(name = "product")
public class Item extends BaseUuidV7Entity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "business_id", nullable = false)
    private Business business;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id")
    private BusinessBranch branch;

    @Column(nullable = false)
    private String name;

    @Column(name = "category_label")
    private String categoryLabel;

    private String description;

    @ElementCollection
    @CollectionTable(name = "product_tag", joinColumns = @JoinColumn(name = "product_id"))
    @Column(name = "tag", nullable = false)
    private List<String> tags;

    private BigDecimal price;

    @Column(nullable = false)
    private Boolean isEnabled;

    @Column(name = "moderation_status", nullable = false)
    @Enumerated(EnumType.STRING)
    private ProductModerationStatus moderationStatus;

    @Column(name = "moderation_note")
    private String moderationNote;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "JSONB")
    private Map<String, Object> attributes;
}

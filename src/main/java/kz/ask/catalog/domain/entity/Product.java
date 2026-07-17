package kz.ask.catalog.domain.entity;

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
import jakarta.persistence.Version;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import kz.ask.business.domain.entity.Business;
import kz.ask.business.domain.entity.Category;
import kz.ask.shared.domain.entity.BaseUuidV7Entity;
import kz.ask.shared.domain.enums.RecordStatus;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Getter
@Setter
@Table(name = "product")
public class Product extends BaseUuidV7Entity {

    @Version
    @Column(name = "entity_version", nullable = false)
    private Long entityVersion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "business_id", nullable = false)
    private Business business;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @Column(nullable = false)
    private String name;

    @Column(name = "category_label")
    private String categoryLabel;

    private String description;

    private String sku;

    @ElementCollection
    @CollectionTable(name = "product_tag", joinColumns = @JoinColumn(name = "product_id"))
    @Column(name = "tag", nullable = false)
    private List<String> tags = new ArrayList<>();

    @Column(name = "characteristics_json", columnDefinition = "TEXT")
    private String characteristicsJson;

    @Column(name = "image_url")
    private String imageUrl;

    private BigDecimal price;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private RecordStatus status;

    @Column(name = "hidden_by_moderator", nullable = false)
    private Boolean hiddenByModerator = Boolean.FALSE;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "JSONB")
    private Map<String, Object> attributes = new HashMap<>();
}

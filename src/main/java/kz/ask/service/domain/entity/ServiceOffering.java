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
import jakarta.persistence.Version;
import java.util.HashMap;
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
@Table(name = "service_offering")
public class ServiceOffering extends BaseUuidV7Entity {

    @Version
    @Column(name = "entity_version", nullable = false)
    private Long entityVersion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "business_id", nullable = false)
    private Business business;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private RecordStatus status;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "JSONB")
    private Map<String, Object> attributes = new HashMap<>();
}

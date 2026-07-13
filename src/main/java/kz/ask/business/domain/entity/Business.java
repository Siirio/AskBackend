package kz.ask.business.domain.entity;

import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.util.List;
import kz.ask.shared.domain.entity.BaseUuidV7Entity;
import kz.ask.shared.domain.enums.RecordStatus;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Getter
@Setter
@Table(name = "business")
public class Business extends BaseUuidV7Entity {

    @Column(nullable = false)
    private String name;

    private String legalName;

    private String bin;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private RecordStatus status;

    @Column(length = 3, columnDefinition = "VARCHAR(3) DEFAULT 'KZT'")
    private String currency;

    @Column(name = "shipping_mode", length = 20)
    private String shippingMode;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "shipping_city_ids", columnDefinition = "JSONB")
    private String shippingCityIds;
}

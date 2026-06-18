package kz.ask.request.domain.entity;

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
import kz.ask.request.domain.enums.SupplierResponseStatus;
import kz.ask.shared.domain.entity.BaseUuidV7Entity;

@Entity
@Getter
@Setter
@Table(name = "supplier_response")
public class SupplierResponse extends BaseUuidV7Entity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "request_target_id", nullable = false)
    private RequestTarget requestTarget;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private SupplierResponseStatus status;

    private BigDecimal price;

    private String productHint;

    private String comment;
}

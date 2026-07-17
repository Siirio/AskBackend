package kz.ask.audit.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import kz.ask.audit.domain.enums.SignificantEventType;
import kz.ask.shared.domain.entity.BaseUuidV7Entity;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Getter
@Setter
@Table(name = "significant_event")
public class SignificantEvent extends BaseUuidV7Entity {

    @Column(name = "actor_user_id")
    private UUID actorUserId;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private SignificantEventType eventType;

    @Column(name = "business_id")
    private UUID businessId;

    @Column(name = "entity_id")
    private UUID entityId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "JSONB")
    private Map<String, Object> metadata = new HashMap<>();
}

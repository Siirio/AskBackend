package kz.ask.search.domain.entity;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import kz.ask.search.domain.enums.SearchAggregateType;
import kz.ask.search.domain.enums.SearchAiMetadataSource;
import kz.ask.search.domain.enums.SearchAiVerificationState;
import kz.ask.shared.domain.entity.BaseUuidV7Entity;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Getter
@Setter
@Table(name = "search_ai_metadata")
public class SearchAiMetadata extends BaseUuidV7Entity {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SearchAggregateType aggregateType;

    @Column(nullable = false)
    private UUID aggregateId;

    @Column(nullable = false, length = 128)
    private String attributeKey;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "JSONB")
    private JsonNode attributeValue;

    @Column(nullable = false, precision = 5, scale = 4)
    private BigDecimal confidence;

    @Column(nullable = false, length = 1000)
    private String evidence;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SearchAiMetadataSource source;

    @Column(nullable = false, length = 128)
    private String modelVersion;

    @Column(nullable = false, length = 128)
    private String schemaVersion;

    @Column(nullable = false)
    private Instant extractedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SearchAiVerificationState verificationState;
}

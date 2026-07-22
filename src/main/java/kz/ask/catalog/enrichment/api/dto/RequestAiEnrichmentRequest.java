package kz.ask.catalog.enrichment.api.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.Set;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RequestAiEnrichmentRequest {

    @NotNull
    private AiEnrichmentTargetType targetType;

    @NotEmpty
    private Set<UUID> aggregateIds;
}

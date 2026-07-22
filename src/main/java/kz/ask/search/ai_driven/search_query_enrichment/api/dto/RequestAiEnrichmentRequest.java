package kz.ask.search.ai_driven.search_query_enrichment.api.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.Set;
import java.util.UUID;
import kz.ask.search.basic.domain.enums.SearchDocumentType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RequestAiEnrichmentRequest {

    @NotNull
    private SearchDocumentType documentType;

    @NotEmpty
    private Set<UUID> aggregateIds;
}

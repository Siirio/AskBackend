package kz.ask.search.basic.infrastructure.mapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import kz.ask.search.basic.domain.dto.MeilisearchIndexDocument;
import kz.ask.search.basic.domain.dto.SearchDocumentDto;
import kz.ask.search.basic.domain.entity.SearchDocument;
import org.springframework.stereotype.Component;

@Component
public class MeilisearchDocumentMapper {

    public MeilisearchIndexDocument toIndexDocument(SearchDocument document) {
        String city = document.getBranch() != null && document.getBranch().getCity() != null
                ? document.getBranch().getCity().getName()
                : "";
        String country = document.getBranch() != null && document.getBranch().getCity() != null
                ? document.getBranch().getCity().getCountryCode()
                : "";
        return new MeilisearchIndexDocument(
                document.getAggregateId().toString(),
                document.getAggregateId().toString(),
                document.getTitle(),
                document.getNormalizedTitle(),
                document.getSummary(),
                document.getAiSearchSummary(),
                document.getAliases(),
                copy(document.getConceptIds()),
                copy(document.getUseCases()),
                document.getSemanticSummary(),
                document.getEmbeddingText(),
                document.getBrand(),
                document.getCategoryPath(),
                document.getCategoryLabel(),
                document.getBusinessName(),
                document.getBranchName(),
                document.getTokens() == null ? List.of() : new ArrayList<>(document.getTokens()),
                document.getPrice(),
                document.getCurrency(),
                document.getLatitude(),
                document.getLongitude(),
                city,
                country,
                document.getDocumentType().name(),
                copy(document.getVerifiedAttributes()),
                copy(document.getAiAttributes()),
                document.getAvailabilityStatus() != null ? document.getAvailabilityStatus().name() : "UNKNOWN",
                document.getProjectionVersion(),
                null);
    }

    public MeilisearchIndexDocument toIndexDocument(SearchDocumentDto document) {
        return new MeilisearchIndexDocument(
                document.getAggregateId().toString(),
                document.getAggregateId().toString(),
                document.getTitle(),
                document.getNormalizedTitle(),
                document.getSummary(),
                null,
                document.getAliases(),
                copy(document.getConceptIds()),
                copy(document.getUseCases()),
                document.getSemanticSummary(),
                document.getEmbeddingText(),
                null,
                null,
                document.getCategoryLabel(),
                document.getBusinessName(),
                document.getBranchName(),
                document.getTokens() == null ? List.of() : new ArrayList<>(document.getTokens()),
                document.getPrice(),
                document.getCurrency(),
                document.getLatitude(),
                document.getLongitude(),
                document.getCity(),
                document.getCountry(),
                document.getDocumentType().name(),
                copy(document.getVerifiedAttributes()),
                copy(document.getAiAttributes()),
                document.getAvailabilityStatus().name(),
                document.getProjectionVersion(),
                null);
    }

    private Map<String, Object> copy(Map<String, Object> attributes) {
        return attributes == null ? Map.of() : new HashMap<>(attributes);
    }

    private List<String> copy(List<String> values) {
        return values == null ? List.of() : new ArrayList<>(values);
    }
}

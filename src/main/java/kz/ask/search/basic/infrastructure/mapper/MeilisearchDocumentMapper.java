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
        return new MeilisearchIndexDocument(
                document.getAggregateId().toString(),
                document.getAggregateId().toString(),
                document.getTitle(),
                document.getNormalizedTitle(),
                document.getSummary(),
                document.getAiSearchSummary(),
                document.getAliases(),
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
}

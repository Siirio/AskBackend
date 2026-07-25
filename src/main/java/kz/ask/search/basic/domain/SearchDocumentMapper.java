package kz.ask.search.basic.domain;

import java.util.Map;
import kz.ask.search.basic.domain.entity.SearchDocument;
import kz.ask.search.basic.domain.enums.SearchAvailabilitySource;
import kz.ask.search.basic.domain.enums.SearchAvailabilityStatus;
import org.springframework.stereotype.Component;

@Component
public class SearchDocumentMapper {

    private static final String CURRENCY_KZT = "KZT";
    private static final String SOURCE = "ITEMS_SERVICES";

    public void populateFromItem(SearchDocument document, SearchableItemSource source) {
        document.setTitle(source.getName());
        document.setNormalizedTitle(SearchTextNormalizer.normalize(source.getName()));
        document.setSummary(source.getDescription());
        document.setCategoryLabel(source.getCategoryLabel());
        document.setBusinessName(source.getBusinessName());
        document.setBranchName(source.getBranchName());
        document.setPrice(source.getPrice());
        document.setCurrency(CURRENCY_KZT);
        document.setLatitude(source.getLatitude());
        document.setLongitude(source.getLongitude());
        document.setTokens(SearchTextNormalizer.tokenize(
                source.getName(), source.getDescription(), source.getTags(), source.getBusinessName()));
        document.setVerifiedAttributes(source.getAttributes() != null ? source.getAttributes() : Map.of());
        document.setAiAttributes(Map.of());
        document.setAliases(source.getTags() != null && !source.getTags().isEmpty()
                ? String.join(", ", source.getTags()) : "");
        document.setSource(SOURCE);
        document.setAvailabilityStatus(SearchAvailabilityStatus.UNKNOWN);
        document.setAvailabilitySource(SearchAvailabilitySource.UNKNOWN);
    }

    public void populateFromService(SearchDocument document, SearchableServiceSource source) {
        document.setTitle(source.getName());
        document.setNormalizedTitle(SearchTextNormalizer.normalize(source.getName()));
        document.setSummary(source.getDescription());
        document.setCategoryLabel(source.getCategoryLabel());
        document.setBusinessName(source.getBusinessName());
        document.setBranchName(source.getBranchName());
        document.setPrice(source.getBasePrice());
        document.setCurrency(CURRENCY_KZT);
        document.setLatitude(source.getLatitude());
        document.setLongitude(source.getLongitude());
        document.setTokens(SearchTextNormalizer.tokenize(
                source.getName(), source.getDescription(), null, source.getBusinessName()));
        document.setVerifiedAttributes(source.getAttributes() != null ? source.getAttributes() : Map.of());
        document.setAiAttributes(Map.of());
        document.setAliases("");
        document.setSource(SOURCE);
        document.setAvailabilityStatus(SearchAvailabilityStatus.UNKNOWN);
        document.setAvailabilitySource(SearchAvailabilitySource.UNKNOWN);
    }
}

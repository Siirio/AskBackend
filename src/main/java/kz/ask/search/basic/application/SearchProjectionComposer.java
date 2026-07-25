package kz.ask.search.basic.application;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import kz.ask.search.basic.domain.SearchTextNormalizer;
import kz.ask.search.basic.domain.dto.SearchDocumentDto;
import kz.ask.search.basic.domain.enums.SearchAvailabilitySource;
import kz.ask.search.basic.domain.enums.SearchAvailabilityStatus;
import kz.ask.search.basic.domain.enums.SearchDocumentType;
import org.springframework.stereotype.Component;

@Component
public class SearchProjectionComposer {

    private static final String SOURCE_ITEMS_SERVICES = "ITEMS_SERVICES";

    public SearchDocumentDto composeItem(UUID itemId, UUID businessId, UUID branchId,
                                          String name, String description,
                                          String categoryLabel, String businessName,
                                          String branchName, BigDecimal price,
                                          String currency, List<String> tags,
                                          Map<String, Object> attributes,
                                          BigDecimal latitude, BigDecimal longitude,
                                          Boolean active) {
        String safeName = name != null ? name : "";
        String safeDescription = description != null ? description : "";
        String safeBusinessName = businessName != null ? businessName : "";
        List<String> safeTags = tags != null ? tags : Collections.emptyList();
        Map<String, Object> safeAttributes = attributes != null ? attributes : Collections.emptyMap();
        String safeCurrency = currency != null ? currency : "KZT";

        return SearchDocumentDto.builder()
                .documentType(SearchDocumentType.ITEM)
                .aggregateId(itemId)
                .businessId(businessId)
                .branchId(branchId)
                .title(safeName)
                .normalizedTitle(SearchTextNormalizer.normalize(safeName))
                .summary(safeDescription)
                .categoryLabel(categoryLabel)
                .businessName(safeBusinessName)
                .branchName(branchName)
                .price(price)
                .currency(safeCurrency)
                .latitude(latitude)
                .longitude(longitude)
                .tokens(SearchTextNormalizer.tokenize(safeName, safeDescription, safeTags, safeBusinessName))
                .aliases(safeTags.isEmpty() ? "" : String.join(", ", safeTags))
                .verifiedAttributes(safeAttributes)
                .aiAttributes(Collections.emptyMap())
                .source(SOURCE_ITEMS_SERVICES)
                .availabilityStatus(SearchAvailabilityStatus.UNKNOWN)
                .availabilitySource(SearchAvailabilitySource.UNKNOWN)
                .build();
    }

    public SearchDocumentDto composeService(UUID serviceOfferingId, UUID businessId, UUID branchId,
                                             String name, String description,
                                             String categoryLabel, String businessName,
                                             String branchName, BigDecimal basePrice,
                                             String currency, Map<String, Object> attributes,
                                             BigDecimal latitude, BigDecimal longitude,
                                             Boolean active) {
        String safeName = name != null ? name : "";
        String safeDescription = description != null ? description : "";
        String safeBusinessName = businessName != null ? businessName : "";
        Map<String, Object> safeAttributes = attributes != null ? attributes : Collections.emptyMap();
        String safeCurrency = currency != null ? currency : "KZT";

        return SearchDocumentDto.builder()
                .documentType(SearchDocumentType.SERVICE)
                .aggregateId(serviceOfferingId)
                .businessId(businessId)
                .branchId(branchId)
                .title(safeName)
                .normalizedTitle(SearchTextNormalizer.normalize(safeName))
                .summary(safeDescription)
                .categoryLabel(categoryLabel)
                .businessName(safeBusinessName)
                .branchName(branchName)
                .price(basePrice)
                .currency(safeCurrency)
                .latitude(latitude)
                .longitude(longitude)
                .tokens(SearchTextNormalizer.tokenize(safeName, safeDescription, null, safeBusinessName))
                .aliases("")
                .verifiedAttributes(safeAttributes)
                .aiAttributes(Collections.emptyMap())
                .source(SOURCE_ITEMS_SERVICES)
                .availabilityStatus(SearchAvailabilityStatus.UNKNOWN)
                .availabilitySource(SearchAvailabilitySource.UNKNOWN)
                .build();
    }

    public SearchDocumentDto composeBusiness(UUID businessId, String name, String description,
                                              String categoryLabel) {
        String safeName = name != null ? name : "";
        String safeDescription = description != null ? description : "";

        return SearchDocumentDto.builder()
                .documentType(SearchDocumentType.BUSINESS)
                .aggregateId(businessId)
                .businessId(businessId)
                .branchId(null)
                .title(safeName)
                .normalizedTitle(SearchTextNormalizer.normalize(safeName))
                .summary(safeDescription)
                .categoryLabel(categoryLabel)
                .businessName(safeName)
                .branchName(null)
                .price(null)
                .currency(null)
                .latitude(null)
                .longitude(null)
                .tokens(SearchTextNormalizer.tokenize(safeName, safeDescription, null, safeName))
                .aliases("")
                .verifiedAttributes(Collections.emptyMap())
                .aiAttributes(Collections.emptyMap())
                .source(SOURCE_ITEMS_SERVICES)
                .availabilityStatus(SearchAvailabilityStatus.UNKNOWN)
                .availabilitySource(SearchAvailabilitySource.UNKNOWN)
                .build();
    }

    public SearchDocumentDto composeUniqueOffer(UUID offerId, UUID businessId, String name,
                                                 String description, String type, BigDecimal price,
                                                 List<String> tags) {
        String safeName = name != null ? name : "";
        String safeDescription = description != null ? description : "";
        List<String> safeTags = tags != null ? tags : Collections.emptyList();

        return SearchDocumentDto.builder()
                .documentType(SearchDocumentType.UNIQUE_OFFER)
                .aggregateId(offerId)
                .businessId(businessId)
                .branchId(null)
                .title(safeName)
                .normalizedTitle(SearchTextNormalizer.normalize(safeName))
                .summary(safeDescription)
                .categoryLabel(type)
                .businessName(null)
                .branchName(null)
                .price(price)
                .currency("KZT")
                .latitude(null)
                .longitude(null)
                .tokens(SearchTextNormalizer.tokenize(safeName, safeDescription, safeTags, ""))
                .aliases(safeTags.isEmpty() ? "" : String.join(", ", safeTags))
                .verifiedAttributes(Collections.emptyMap())
                .aiAttributes(Collections.emptyMap())
                .source(SOURCE_ITEMS_SERVICES)
                .availabilityStatus(SearchAvailabilityStatus.UNKNOWN)
                .availabilitySource(SearchAvailabilitySource.UNKNOWN)
                .build();
    }
}

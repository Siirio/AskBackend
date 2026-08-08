package kz.ask.search.basic.application;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import kz.ask.search.basic.domain.SearchTextNormalizer;
import kz.ask.search.basic.domain.dto.SearchDocumentDto;
import kz.ask.search.basic.domain.enums.SearchAvailabilitySource;
import kz.ask.search.basic.domain.enums.SearchAvailabilityStatus;
import kz.ask.search.basic.domain.enums.SearchDocumentType;
import kz.ask.search.basic.domain.enums.SearchProjectionAction;
import kz.ask.shared.error.ErrorCode;
import kz.ask.shared.error.ValidationException;
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
                                          BigDecimal latitude, BigDecimal longitude) {
        String safeName = requireText(name);
        String safeDescription = description != null ? description : "";
        String safeBusinessName = requireText(businessName);
        List<String> safeTags = distinct(tags);
        Map<String, Object> safeAttributes = attributes != null ? attributes : Collections.emptyMap();
        String attributeText = buildAttributeText(safeAttributes);

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
                .currency(requireText(currency))
                .latitude(latitude)
                .longitude(longitude)
                .tokens(SearchTextNormalizer.tokenize(
                        safeName, safeDescription, categoryLabel, safeTags, safeBusinessName, attributeText))
                .embeddingText(buildEmbeddingText(
                        safeName, safeDescription, categoryLabel, safeTags, safeBusinessName, attributeText))
                .verifiedAttributes(safeAttributes)
                .source(SOURCE_ITEMS_SERVICES)
                .availabilityStatus(SearchAvailabilityStatus.UNKNOWN)
                .availabilitySource(SearchAvailabilitySource.UNKNOWN)
                .projectionAction(SearchProjectionAction.INDEX)
                .build();
    }

    public SearchDocumentDto composeService(UUID serviceOfferingId, UUID businessId, UUID branchId,
                                             String name, String description,
                                             String categoryLabel, String businessName,
                                             String branchName, BigDecimal basePrice,
                                             String currency, Map<String, Object> attributes,
                                             BigDecimal latitude, BigDecimal longitude) {
        String safeName = requireText(name);
        String safeDescription = description != null ? description : "";
        String safeBusinessName = requireText(businessName);
        Map<String, Object> safeAttributes = attributes != null ? attributes : Collections.emptyMap();
        String attributeText = buildAttributeText(safeAttributes);

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
                .currency(requireText(currency))
                .latitude(latitude)
                .longitude(longitude)
                .tokens(SearchTextNormalizer.tokenize(
                        safeName, safeDescription, categoryLabel, List.of(), safeBusinessName, attributeText))
                .embeddingText(buildEmbeddingText(
                        safeName, safeDescription, categoryLabel, List.of(), safeBusinessName, attributeText))
                .verifiedAttributes(safeAttributes)
                .source(SOURCE_ITEMS_SERVICES)
                .availabilityStatus(SearchAvailabilityStatus.UNKNOWN)
                .availabilitySource(SearchAvailabilitySource.UNKNOWN)
                .projectionAction(SearchProjectionAction.INDEX)
                .build();
    }

    private String buildEmbeddingText(String title, String description, String category,
                                       List<String> tags, String businessName) {
        return buildEmbeddingText(title, description, category, tags, businessName, "");
    }

    private String buildEmbeddingText(String title, String description, String category,
                                       List<String> tags, String businessName, String attributeText) {
        return String.join(". ",
                        title,
                        description != null ? description : "",
                        category != null ? category : "",
                        tags.isEmpty() ? "" : String.join(", ", tags),
                        businessName,
                        attributeText)
                .replaceAll("\\.\\s*\\.", ".")
                .trim();
    }

    private String buildAttributeText(Map<String, Object> attributes) {
        if (attributes == null || attributes.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Object> entry : attributes.entrySet()) {
            Object value = entry.getValue();
            if (value == null) {
                continue;
            }
            String key = entry.getKey();
            if (value instanceof List<?> list) {
                for (Object item : list) {
                    if (item != null) {
                        sb.append(key).append(" ").append(item).append(". ");
                    }
                }
            } else {
                sb.append(key).append(" ").append(value).append(". ");
            }
        }
        return sb.toString().trim();
    }

    private String requireText(String value) {
        if (value == null || value.isBlank()) {
            throw new ValidationException(ErrorCode.SEARCH_PROJECTION_INVALID);
        }
        return value.trim();
    }

    private List<String> distinct(List<String> values) {
        if (values == null) {
            return List.of();
        }
        return values.stream()
                .filter(value -> value != null && !value.isBlank())
                .map(String::trim)
                .distinct()
                .toList();
    }
}

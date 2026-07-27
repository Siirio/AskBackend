package kz.ask.search.basic.application;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Collections;
import java.util.HexFormat;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
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
    private static final String SEMANTIC_SCHEMA_VERSION = "1";

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
                        safeName, safeDescription, categoryLabel, safeTags, safeBusinessName))
                .aliases(safeTags.isEmpty() ? "" : String.join(", ", safeTags))
                .conceptIds(List.of())
                .useCases(List.of())
                .semanticSummary(safeDescription)
                .embeddingText(embeddingText(
                        safeName, safeDescription, categoryLabel, safeTags, List.of(), List.of()))
                .semanticEvidence(List.of())
                .semanticSchemaVersion(SEMANTIC_SCHEMA_VERSION)
                .semanticSourceHash(sourceHash(safeName, safeDescription, categoryLabel, safeTags))
                .verifiedAttributes(safeAttributes)
                .aiAttributes(Collections.emptyMap())
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
                        safeName, safeDescription, categoryLabel, List.of(), safeBusinessName))
                .aliases(String.join(", ", List.of()))
                .conceptIds(List.of())
                .useCases(List.of())
                .semanticSummary(safeDescription)
                .embeddingText(embeddingText(
                        safeName, safeDescription, categoryLabel, List.of(), List.of(), List.of()))
                .semanticEvidence(List.of())
                .semanticSchemaVersion(SEMANTIC_SCHEMA_VERSION)
                .semanticSourceHash(sourceHash(safeName, safeDescription, categoryLabel, List.of()))
                .verifiedAttributes(safeAttributes)
                .aiAttributes(Collections.emptyMap())
                .source(SOURCE_ITEMS_SERVICES)
                .availabilityStatus(SearchAvailabilityStatus.UNKNOWN)
                .availabilitySource(SearchAvailabilitySource.UNKNOWN)
                .projectionAction(SearchProjectionAction.INDEX)
                .build();
    }

    public SearchDocumentDto applySemanticMetadata(SearchDocumentDto projection,
                                                    List<String> aliases,
                                                    List<String> conceptIds,
                                                    List<String> useCases,
                                                    String semanticSummary,
                                                    BigDecimal confidence,
                                                    List<String> evidence,
                                                    String modelVersion,
                                                    Instant generatedAt) {
        List<String> safeAliases = distinct(aliases);
        List<String> safeConceptIds = distinct(conceptIds);
        List<String> safeUseCases = distinct(useCases);
        List<String> safeEvidence = distinct(evidence);
        String safeSummary = semanticSummary == null || semanticSummary.isBlank()
                ? projection.getSummary() : semanticSummary.trim();
        List<String> tokens = SearchTextNormalizer.tokenize(
                projection.getTitle(),
                String.join(" ", projection.getSummary(), safeSummary),
                projection.getCategoryLabel(),
                concatLists(safeAliases, safeConceptIds, safeUseCases),
                projection.getBusinessName());
        return projection.toBuilder()
                .tokens(tokens)
                .aliases(String.join(", ", safeAliases))
                .conceptIds(safeConceptIds)
                .useCases(safeUseCases)
                .semanticSummary(safeSummary)
                .embeddingText(embeddingText(
                        projection.getTitle(), safeSummary, projection.getCategoryLabel(),
                        safeAliases, safeConceptIds, safeUseCases))
                .semanticConfidence(confidence)
                .semanticEvidence(safeEvidence)
                .semanticModelVersion(modelVersion)
                .semanticSchemaVersion(SEMANTIC_SCHEMA_VERSION)
                .semanticMetadataSourceHash(projection.getSemanticSourceHash())
                .semanticGeneratedAt(generatedAt)
                .build();
    }

    private String requireText(String value) {
        if (value == null || value.isBlank()) {
            throw new ValidationException(ErrorCode.SEARCH_PROJECTION_INVALID);
        }
        return value.trim();
    }

    private String embeddingText(String title, String summary, String category,
                                 List<String> aliases, List<String> conceptIds, List<String> useCases) {
        return concatLists(
                        List.of(title, summary == null ? "" : summary, category == null ? "" : category),
                        aliases, conceptIds, useCases)
                .stream()
                .filter(value -> value != null && !value.isBlank())
                .map(String::trim)
                .distinct()
                .collect(java.util.stream.Collectors.joining(". "));
    }

    private String sourceHash(String title, String description, String category, List<String> aliases) {
        String source = String.join("\n",
                title,
                description == null ? "" : description,
                category == null ? "" : category,
                String.join("\n", aliases));
        try {
            return HexFormat.of().formatHex(
                    MessageDigest.getInstance("SHA-256").digest(source.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException failure) {
            throw new IllegalStateException(failure);
        }
    }

    private List<String> distinct(List<String> values) {
        if (values == null) {
            return List.of();
        }
        LinkedHashSet<String> result = new LinkedHashSet<>();
        values.stream()
                .filter(value -> value != null && !value.isBlank())
                .map(String::trim)
                .forEach(result::add);
        return List.copyOf(result);
    }

    @SafeVarargs
    private final List<String> concatLists(List<String>... values) {
        return java.util.Arrays.stream(values)
                .filter(java.util.Objects::nonNull)
                .flatMap(List::stream)
                .toList();
    }
}

package kz.ask.search.decision.application.processor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import kz.ask.search.basic.domain.SearchDocumentService;
import kz.ask.search.basic.domain.dto.SearchDocumentDto;
import kz.ask.search.basic.domain.enums.SearchDocumentType;
import kz.ask.search.basic.domain.enums.SearchScope;
import kz.ask.search.decision.api.dto.CompareGroupResponse;
import kz.ask.search.decision.api.dto.CompareItemResponse;
import kz.ask.search.decision.api.dto.CompareResponse;
import kz.ask.search.decision.api.dto.CompareRowResponse;
import kz.ask.search.decision.api.dto.CompareValueResponse;
import kz.ask.shared.error.ErrorCode;
import kz.ask.shared.error.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CompareProcessor {

    private final SearchDocumentService searchDocumentService;

    public CompareResponse compare(SearchScope mode, List<UUID> resultIds, String locale) {
        if (resultIds == null || resultIds.size() < 2 || resultIds.size() > 5) {
            throw new ValidationException(ErrorCode.COMPARE_INVALID_COUNT);
        }
        if (resultIds.stream().distinct().count() != resultIds.size()) {
            throw new ValidationException(ErrorCode.COMPARE_DUPLICATE_IDS);
        }

        SearchDocumentType docType = mode == SearchScope.ITEM
                ? SearchDocumentType.ITEM : SearchDocumentType.SERVICE;
        Map<UUID, SearchDocumentDto> docsById = searchDocumentService
                .findSearchableByAggregateIds(docType, resultIds).stream()
                .collect(java.util.stream.Collectors.toMap(
                        SearchDocumentDto::getAggregateId, d -> d, (a, b) -> a));

        for (UUID id : resultIds) {
            if (!docsById.containsKey(id)) {
                throw new ValidationException(ErrorCode.COMPARE_RESULT_NOT_FOUND);
            }
        }

        List<SearchDocumentDto> docs = resultIds.stream().map(docsById::get).toList();
        List<CompareItemResponse> items = docs.stream()
                .map(doc -> CompareItemResponse.builder()
                        .resultId(doc.getAggregateId())
                        .title(doc.getTitle())
                        .price(doc.getPrice())
                        .currency(doc.getCurrency())
                        .verdict(buildVerdict(doc, docs))
                        .build())
                .toList();

        List<CompareGroupResponse> groups = buildComparisonGroups(docs);

        return CompareResponse.builder()
                .mode(mode.name())
                .items(items)
                .groups(groups)
                .build();
    }

    private List<CompareGroupResponse> buildComparisonGroups(List<SearchDocumentDto> docs) {
        List<CompareGroupResponse> groups = new ArrayList<>();

        groups.add(buildMainGroup(docs));
        if (docs.stream().anyMatch(d -> d.getVerifiedAttributes() != null
                && !d.getVerifiedAttributes().isEmpty())) {
            groups.add(buildAttributesGroup(docs));
        }

        return groups;
    }

    private CompareGroupResponse buildMainGroup(List<SearchDocumentDto> docs) {
        List<CompareRowResponse> rows = new ArrayList<>();

        rows.add(buildRow("price", "Цена", docs.stream()
                .map(doc -> CompareValueResponse.builder()
                        .resultId(doc.getAggregateId())
                        .value(formatPrice(doc.getPrice(), doc.getCurrency()))
                        .status("KNOWN")
                        .build())
                .toList()));

        rows.add(buildRow("category", "Категория", docs.stream()
                .map(doc -> CompareValueResponse.builder()
                        .resultId(doc.getAggregateId())
                        .value(doc.getCategoryLabel())
                        .status("KNOWN")
                        .build())
                .toList()));

        rows.add(buildRow("business", "Компания", docs.stream()
                .map(doc -> CompareValueResponse.builder()
                        .resultId(doc.getAggregateId())
                        .value(doc.getBusinessName())
                        .status("KNOWN")
                        .build())
                .toList()));

        if (docs.stream().anyMatch(d -> d.getCity() != null && !d.getCity().isBlank())) {
            rows.add(buildRow("city", "Город", docs.stream()
                    .map(doc -> CompareValueResponse.builder()
                            .resultId(doc.getAggregateId())
                            .value(doc.getCity())
                            .status("KNOWN")
                            .build())
                    .toList()));
        }

        return CompareGroupResponse.builder()
                .key("main")
                .label("Основное")
                .rows(rows)
                .build();
    }

    private CompareGroupResponse buildAttributesGroup(List<SearchDocumentDto> docs) {
        Map<String, List<CompareValueResponse>> attrRows = new LinkedHashMap<>();
        for (SearchDocumentDto doc : docs) {
            Map<String, Object> attrs = doc.getVerifiedAttributes();
            if (attrs == null) {
                continue;
            }
            for (Map.Entry<String, Object> entry : attrs.entrySet()) {
                String value = formatAttributeValue(entry.getValue());
                attrRows.computeIfAbsent(entry.getKey(), k -> new ArrayList<>())
                        .add(CompareValueResponse.builder()
                                .resultId(doc.getAggregateId())
                                .value(value)
                                .status("KNOWN")
                                .build());
            }
        }

        List<CompareRowResponse> rows = new ArrayList<>();
        for (Map.Entry<String, List<CompareValueResponse>> entry : attrRows.entrySet()) {
            Map<UUID, CompareValueResponse> byId = new LinkedHashMap<>();
            for (CompareValueResponse v : entry.getValue()) {
                byId.put(v.getResultId(), v);
            }
            List<CompareValueResponse> rowValues = docs.stream()
                    .map(doc -> byId.getOrDefault(doc.getAggregateId(),
                            CompareValueResponse.builder()
                                    .resultId(doc.getAggregateId())
                                    .value("—")
                                    .status("UNKNOWN")
                                    .build()))
                    .toList();
            rows.add(buildRow(entry.getKey(), entry.getKey(), rowValues));
        }

        return CompareGroupResponse.builder()
                .key("attributes")
                .label("Характеристики")
                .rows(rows)
                .build();
    }

    private CompareRowResponse buildRow(String key, String label, List<CompareValueResponse> values) {
        boolean isDifferent = values.stream()
                .map(CompareValueResponse::getValue)
                .distinct()
                .count() > 1;
        List<CompareValueResponse> withHighlights = applyHighlights(key, values);
        return CompareRowResponse.builder()
                .key(key)
                .label(label)
                .isDifferent(isDifferent)
                .values(withHighlights)
                .build();
    }

    private List<CompareValueResponse> applyHighlights(String key, List<CompareValueResponse> values) {
        if (!isHighlightableKey(key) || values.size() < 2) {
            return values;
        }
        if ("price".equals(key)) {
            int bestIdx = findBestPriceIndex(values);
            if (bestIdx >= 0) {
                return markHighlight(values, bestIdx);
            }
        }
        return values;
    }

    private boolean isHighlightableKey(String key) {
        return "price".equals(key);
    }

    private int findBestPriceIndex(List<CompareValueResponse> values) {
        int bestIdx = -1;
        BigDecimal bestPrice = null;
        for (int i = 0; i < values.size(); i++) {
            String text = values.get(i).getValue();
            if (text == null || text.equals("—")) {
                continue;
            }
            try {
                String clean = text.replaceAll("[^\\d.]", "").trim();
                if (clean.isEmpty()) {
                    continue;
                }
                BigDecimal price = new BigDecimal(clean);
                if (bestPrice == null || price.compareTo(bestPrice) < 0) {
                    bestPrice = price;
                    bestIdx = i;
                }
            } catch (NumberFormatException ignored) {
                continue;
            }
        }
        return bestIdx;
    }

    private List<CompareValueResponse> markHighlight(List<CompareValueResponse> values, int highlightIdx) {
        List<CompareValueResponse> result = new ArrayList<>();
        for (int i = 0; i < values.size(); i++) {
            CompareValueResponse original = values.get(i);
            result.add(CompareValueResponse.builder()
                    .resultId(original.getResultId())
                    .value(original.getValue())
                    .status(original.getStatus())
                    .highlight(i == highlightIdx ? "BEST" : null)
                    .build());
        }
        return result;
    }

    private String buildVerdict(SearchDocumentDto doc, List<SearchDocumentDto> allDocs) {
        BigDecimal minPrice = allDocs.stream()
                .map(SearchDocumentDto::getPrice)
                .filter(Objects::nonNull)
                .min(BigDecimal::compareTo)
                .orElse(null);
        if (doc.getPrice() == null) {
            return null;
        }
        if (minPrice != null && doc.getPrice().compareTo(minPrice) == 0 && allDocs.size() > 1) {
            return "Самый доступный вариант";
        }
        return null;
    }

    private String formatPrice(BigDecimal price, String currency) {
        if (price == null) {
            return "—";
        }
        String symbol = "KZT".equalsIgnoreCase(currency) ? "₸" : currency;
        return String.format("%,.0f %s", price, symbol).replace(",", " ");
    }

    private String formatAttributeValue(Object value) {
        if (value == null) {
            return "—";
        }
        if (value instanceof List<?> list) {
            return list.stream()
                    .map(Object::toString)
                    .reduce((a, b) -> a + ", " + b)
                    .orElse("—");
        }
        return value.toString();
    }
}

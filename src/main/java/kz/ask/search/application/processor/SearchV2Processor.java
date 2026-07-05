package kz.ask.search.application.processor;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import kz.ask.contact.domain.ContactActionService;
import kz.ask.search.api.dto.SearchResultCardResponse;
import kz.ask.search.api.dto.SearchV2CardResponse;
import kz.ask.search.api.dto.SearchV2Request;
import kz.ask.search.api.dto.SearchV2Response;
import kz.ask.search.api.dto.SearchV2SectionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SearchV2Processor {

    private static final Integer RESULT_LIMIT = 30;
    private static final String SORT_PRICE_ASC = "price_asc";
    private static final String SORT_PRICE_DESC = "price_desc";

    private final PublicSearchProcessor publicSearchProcessor;
    private final ContactActionService contactActionService;

    public SearchV2Response search(SearchV2Request request) {
        List<SearchResultCardResponse> cards = publicSearchProcessor.search(
                request.getRawQuery(),
                request.getScope(),
                request.getSelectedCategory(),
                0,
                RESULT_LIMIT);
        List<SearchV2CardResponse> v2Cards = sort(cards, request.getSort()).stream()
                .map(this::toCard)
                .toList();
        return SearchV2Response.builder()
                .searchSessionId(null)
                .rawQuery(request.getRawQuery())
                .scope(resolveScope(request.getScope()))
                .understoodQuery(request.getRawQuery())
                .sections(groupSections(v2Cards))
                .supplierCheckCount(0)
                .build();
    }

    private List<SearchResultCardResponse> sort(List<SearchResultCardResponse> cards, String sort) {
        if (SORT_PRICE_ASC.equalsIgnoreCase(sort)) {
            return cards.stream()
                    .sorted(Comparator.comparing(this::priceForSort))
                    .toList();
        }
        if (SORT_PRICE_DESC.equalsIgnoreCase(sort)) {
            return cards.stream()
                    .sorted(Comparator.comparing(this::priceForSort).reversed())
                    .toList();
        }
        return cards;
    }

    private BigDecimal priceForSort(SearchResultCardResponse card) {
        return card.getPrice() == null ? BigDecimal.valueOf(Long.MAX_VALUE) : card.getPrice();
    }

    private SearchV2CardResponse toCard(SearchResultCardResponse card) {
        return SearchV2CardResponse.builder()
                .component(component(card.getType()))
                .resultId(card.getId())
                .businessId(card.getBusinessId())
                .businessName(card.getBusinessName())
                .brandColor(card.getBrandColor())
                .brandLogoUrl(card.getBrandLogoUrl())
                .title(card.getName())
                .price(card.getPrice())
                .availability(card.getAvailabilityStatus())
                .matchReasons(card.getMatchReasons())
                .badges(card.getBadges())
                .distanceMeters(card.getDistanceMeters())
                .branchName(card.getBranchName())
                .hasActiveDrop(Boolean.TRUE.equals(card.getHasActiveDrop()))
                .contactActions(card.getBusinessId() == null ? List.of() : contactActionService.summarize(card.getBusinessId()))
                .build();
    }

    private List<SearchV2SectionResponse> groupSections(List<SearchV2CardResponse> cards) {
        Map<String, List<SearchV2CardResponse>> sections = new LinkedHashMap<>();
        sections.put("fresh_drops", filter(cards, "fresh_drops"));
        sections.put("exact_products", filter(cards, "exact_products"));
        sections.put("over_budget", filter(cards, "over_budget"));
        sections.put("needs_confirmation", filter(cards, "needs_confirmation"));
        sections.put("similar_products", filter(cards, "similar_products"));
        return sections.entrySet().stream()
                .filter(entry -> !entry.getValue().isEmpty())
                .map(entry -> SearchV2SectionResponse.builder()
                        .type(entry.getKey())
                        .title(sectionTitle(entry.getKey()))
                        .cards(entry.getValue())
                        .build())
                .toList();
    }

    private List<SearchV2CardResponse> filter(List<SearchV2CardResponse> cards, String sectionType) {
        return cards.stream()
                .filter(card -> sectionType.equals(sectionType(card)))
                .toList();
    }

    private String sectionType(SearchV2CardResponse card) {
        if ("DropCard".equals(card.getComponent())) {
            return "fresh_drops";
        }
        if (card.getPrice() == null && !"ServiceCard".equals(card.getComponent())) {
            return "needs_confirmation";
        }
        return "exact_products";
    }

    private String sectionTitle(String sectionType) {
        return switch (sectionType) {
            case "fresh_drops" -> "Свежие дропы";
            case "over_budget" -> "Похожие варианты дороже бюджета";
            case "needs_confirmation" -> "Нужно уточнить у поставщика";
            case "similar_products" -> "Похожие варианты";
            default -> "Подходит под запрос";
        };
    }

    private String component(String type) {
        if ("SERVICE".equals(type)) {
            return "ServiceCard";
        }
        if ("DROP".equals(type)) {
            return "DropCard";
        }
        return "ProductCard";
    }

    private String resolveScope(String scope) {
        if (scope == null || scope.isBlank()) {
            return "ALL";
        }
        return scope.toUpperCase(Locale.ROOT);
    }
}

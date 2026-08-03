package kz.ask.search.basic.application.processor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import kz.ask.business.profile.domain.BusinessProfileService;
import kz.ask.business.profile.domain.dto.BusinessProfileDto;
import kz.ask.business.uniqueoffer.domain.UniqueOfferService;
import kz.ask.business.uniqueoffer.domain.dto.UniqueOfferBoostDto;
import kz.ask.offer.item.infrastructure.repository.ProductRepository;
import kz.ask.offer.media.CatalogImageMutation;
import kz.ask.offer.media.CatalogImageResponse;
import kz.ask.offer.purchase.api.dto.PurchaseDestinationResponse;
import kz.ask.offer.purchase.infrastructure.mapper.PurchaseDestinationMapper;
import kz.ask.offer.service.infrastructure.repository.ServiceOfferingRepository;
import kz.ask.search.basic.api.dto.SearchBusinessProfileResponse;
import kz.ask.search.basic.api.dto.SearchCardResponse;
import kz.ask.search.basic.api.dto.SearchConstraintResponse;
import kz.ask.search.basic.api.dto.SearchLocationRequest;
import kz.ask.search.basic.api.dto.SearchRequest;
import kz.ask.search.basic.api.dto.SearchResponse;
import kz.ask.search.basic.api.dto.SearchSectionResponse;
import kz.ask.search.basic.domain.SearchDocumentService;
import kz.ask.search.basic.domain.dto.SearchDocumentDto;
import kz.ask.search.basic.domain.dto.SearchHitDto;
import kz.ask.search.basic.domain.enums.SearchAvailabilityStatus;
import kz.ask.search.basic.domain.enums.SearchDocumentType;
import kz.ask.search.basic.domain.enums.SearchScope;
import kz.ask.search.basic.infrastructure.meilisearch.MeilisearchIndexGateway;
import kz.ask.search.search_query_enrichment.api.dto.SearchIntentStructureRequest;
import kz.ask.search.search_query_enrichment.domain.SearchIntentStructurer;
import kz.ask.shared.error.ErrorCode;
import kz.ask.shared.error.InternalServerException;
import kz.ask.shared.util.DistanceCalculator;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.data.domain.Page;

@Slf4j
@Component
@RequiredArgsConstructor
public class StructuredSearchProcessor {

    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final String DEFAULT_BRAND_COLOR = "#0d9b7c";

    private final SearchIntentStructurer searchIntentStructurer;
    private final SearchDocumentService searchDocumentService;
    private final BusinessProfileService businessProfileService;
    private final UniqueOfferService uniqueOfferService;
    private final MeilisearchIndexGateway meilisearchIndexGateway;
    private final ProductRepository productRepository;
    private final ServiceOfferingRepository serviceOfferingRepository;
    private final CatalogImageMutation catalogImageMutation;
    private final PurchaseDestinationMapper purchaseDestinationMapper;

    public SearchResponse search(SearchRequest request) {
        int page = request.getPage() == null ? 0 : request.getPage();
        int pageSize = request.getPageSize() == null ? DEFAULT_PAGE_SIZE : request.getPageSize();

        SearchIntentStructureRequest aiRequest = toIntentRequest(request);
        SearchInterpretation interpretation = searchIntentStructurer.interpret(aiRequest);
        SearchPlan plan = buildSearchPlan(interpretation, request);

        Page<SearchHitDto> hitPage = meilisearchIndexGateway.search(plan, page, pageSize);
        List<SearchHitDto> hits = hitPage.getContent();
        List<SearchDocumentDto> documents = hydrateDocuments(plan, hits);
        List<RankedDocument> ranked = rank(documents, plan, request.getUserLocation());

        Set<UUID> businessIds = ranked.stream()
                .map(r -> r.document.getBusinessId())
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<UUID, BusinessProfileDto> profiles = businessProfileService.findByBusinessIds(businessIds);
        Map<UUID, List<CatalogImageResponse>> images = loadImages(ranked);
        Map<UUID, List<PurchaseDestinationResponse>> purchaseDestinations = loadPurchaseDestinations(ranked);

        return SearchResponse.builder()
                .rawQuery(request.getRawQuery())
                .mode(plan.getItemType().name())
                .understoodQuery(interpretation.getNormalizedQuery() != null
                        ? interpretation.getNormalizedQuery() : request.getRawQuery())
                .interpretedConstraints(toConstraints(plan))
                .sections(toSections(ranked, profiles, images, purchaseDestinations, request.getLocale()))
                .page(page)
                .pageSize(pageSize)
                .total(Math.toIntExact(hitPage.getTotalElements()))
                .hasNext(hitPage.hasNext())
                .ambiguity(interpretation.getAmbiguity())
                .suggestions(interpretation.getSuggestions())
                .build();
    }

    private List<SearchDocumentDto> hydrateDocuments(SearchPlan plan, List<SearchHitDto> hits) {
        List<UUID> ids = hits.stream().map(SearchHitDto::getAggregateId).toList();
        if (ids.isEmpty()) {
            return List.of();
        }
        Map<UUID, SearchDocumentDto> docsById = searchDocumentService
                .findSearchableByAggregateIds(plan.getItemType(), ids).stream()
                .collect(Collectors.toMap(SearchDocumentDto::getAggregateId, d -> d, (a, b) -> a));
        return ids.stream()
                .map(docsById::get)
                .filter(Objects::nonNull)
                .toList();
    }

    private List<RankedDocument> rank(List<SearchDocumentDto> documents,
                                      SearchPlan plan, SearchLocationRequest userLocation) {
        Map<UUID, UniqueOfferBoostDto> itemBoosts = uniqueOfferService.findActiveItemBoosts(
                documents.stream()
                        .filter(d -> d.getDocumentType() == SearchDocumentType.ITEM)
                        .map(SearchDocumentDto::getAggregateId)
                        .toList());
        Map<UUID, UniqueOfferBoostDto> serviceBoosts = uniqueOfferService.findActiveServiceBoosts(
                documents.stream()
                        .filter(d -> d.getDocumentType() == SearchDocumentType.SERVICE)
                        .map(SearchDocumentDto::getAggregateId)
                        .toList());
        Map<UUID, UniqueOfferBoostDto> allBoosts = new HashMap<>();
        allBoosts.putAll(itemBoosts);
        allBoosts.putAll(serviceBoosts);

        return documents.stream()
                .map(doc -> {
                    List<String> warnings = new ArrayList<>();

                    addPriceWarnings(doc, plan, warnings);
                    addCityWarning(doc, plan, warnings);

                    UniqueOfferBoostDto boost = allBoosts.get(doc.getAggregateId());
                    String offerLabel = null;
                    if (boost != null && appliesToBranch(doc, boost)) {
                        offerLabel = boost.getLabel();
                    }

                    Integer distanceMeters = null;
                    if (userLocation != null
                            && doc.getLatitude() != null && doc.getLongitude() != null) {
                        distanceMeters = DistanceCalculator.meters(
                                userLocation.getLat(), userLocation.getLng(),
                                doc.getLatitude().doubleValue(), doc.getLongitude().doubleValue());
                    }

                    return RankedDocument.builder()
                            .document(doc)
                            .warnings(warnings)
                            .distanceMeters(distanceMeters)
                            .activeOfferLabel(offerLabel)
                            .build();
                })
                .toList();
    }

    private void addPriceWarnings(SearchDocumentDto doc, SearchPlan plan, List<String> warnings) {
        if (doc.getPrice() == null) {
            return;
        }
        if (plan.getMaxPrice() != null && doc.getPrice().compareTo(plan.getMaxPrice()) > 0) {
            warnings.add("OVER_BUDGET");
        }
        if (plan.getMinPrice() != null && doc.getPrice().compareTo(plan.getMinPrice()) < 0) {
            warnings.add("UNDER_BUDGET");
        }
    }

    private void addCityWarning(SearchDocumentDto doc, SearchPlan plan, List<String> warnings) {
        String docCity = normalize(doc.getCity());
        boolean hasHardCity = plan.getCity() != null && !plan.getCity().isBlank();
        if (!hasHardCity) {
            return;
        }
        if (docCity.isBlank()) {
            return;
        }
        if (hasHardCity && !docCity.contains(normalize(plan.getCity()))) {
            warnings.add("WRONG_CITY");
        }
    }

    private SearchPlan buildSearchPlan(SearchInterpretation interpretation, SearchRequest request) {
        SearchDocumentType itemType = request.getMode() == SearchScope.ITEM
                ? SearchDocumentType.ITEM : SearchDocumentType.SERVICE;
        return SearchPlan.builder()
                .rawQuery(request.getRawQuery())
                .itemType(itemType)
                .city(request.getExplicitFilters() == null ? null : request.getExplicitFilters().getCity())
                .country(request.getExplicitFilters() == null ? null : request.getExplicitFilters().getCountry())
                .userSelectedCategory(request.getExplicitFilters() == null
                        ? null : request.getExplicitFilters().getCategory())
                .sort(normalizeSort(request.getSort()))
                .minPrice(request.getExplicitFilters() == null ? null : request.getExplicitFilters().getMinPrice())
                .maxPrice(request.getExplicitFilters() == null ? null : request.getExplicitFilters().getMaxPrice())
                .radiusMeters(request.getExplicitFilters() == null ? null : request.getExplicitFilters().getRadiusMeters())
                .businessIds(request.getExplicitFilters() == null ? null : request.getExplicitFilters().getBusinessIds())
                .mapNorth(request.getExplicitFilters() == null || request.getExplicitFilters().getMapArea() == null
                        ? null : request.getExplicitFilters().getMapArea().getNorth())
                .mapSouth(request.getExplicitFilters() == null || request.getExplicitFilters().getMapArea() == null
                        ? null : request.getExplicitFilters().getMapArea().getSouth())
                .mapEast(request.getExplicitFilters() == null || request.getExplicitFilters().getMapArea() == null
                        ? null : request.getExplicitFilters().getMapArea().getEast())
                .mapWest(request.getExplicitFilters() == null || request.getExplicitFilters().getMapArea() == null
                        ? null : request.getExplicitFilters().getMapArea().getWest())
                .activeOfferAggregateIds(activeOfferIds(itemType, request.getSort()))
                .userLatitude(request.getUserLocation() == null ? null : request.getUserLocation().getLat())
                .userLongitude(request.getUserLocation() == null ? null : request.getUserLocation().getLng())
                .inferredMinPrice(interpretation.getInferredMinPrice())
                .inferredMaxPrice(interpretation.getInferredMaxPrice())
                .inferredCity(interpretation.getInferredCity())
                .ambiguity(interpretation.getAmbiguity())
                .clarificationSuggestions(interpretation.getSuggestions())
                .build();
    }

    private SearchIntentStructureRequest toIntentRequest(SearchRequest request) {
        SearchIntentStructureRequest aiRequest = new SearchIntentStructureRequest();
        aiRequest.setRawQuery(request.getRawQuery());
        aiRequest.setSelectedMode(request.getMode().name());
        aiRequest.setSelectedCategory(request.getExplicitFilters() == null
                ? null : request.getExplicitFilters().getCategory());
        aiRequest.setCity(request.getExplicitFilters() == null
                ? null : request.getExplicitFilters().getCity());
        aiRequest.setCountry(request.getExplicitFilters() == null
                ? null : request.getExplicitFilters().getCountry());
        aiRequest.setSort(normalizeSort(request.getSort()));
        aiRequest.setUserLocation(request.getUserLocation());
        aiRequest.setLanguage(request.getLocale());
        aiRequest.setExplicitMinPrice(request.getExplicitFilters() == null
                ? null : request.getExplicitFilters().getMinPrice());
        aiRequest.setExplicitMaxPrice(request.getExplicitFilters() == null
                ? null : request.getExplicitFilters().getMaxPrice());
        aiRequest.setRadiusMeters(request.getExplicitFilters() == null
                ? null : request.getExplicitFilters().getRadiusMeters());
        return aiRequest;
    }

    private List<SearchConstraintResponse> toConstraints(SearchPlan plan) {
        List<SearchConstraintResponse> constraints = new ArrayList<>();
        addConstraint(constraints, "mode", plan.getItemType().name(), "EXPLICIT");
        addConstraint(constraints, "category", plan.getUserSelectedCategory(), "EXPLICIT");
        addConstraint(constraints, "city", plan.getCity(), "EXPLICIT");
        addConstraint(constraints, "min_price", plan.getMinPrice(), "EXPLICIT");
        addConstraint(constraints, "max_price", plan.getMaxPrice(), "EXPLICIT");
        addConstraint(constraints, "country", plan.getCountry(), "EXPLICIT");
        addConstraint(constraints, "radius_meters", plan.getRadiusMeters(), "EXPLICIT");
        addConstraint(constraints, "business_ids", plan.getBusinessIds(), "EXPLICIT");
        addConstraint(constraints, "map_area", mapAreaConstraint(plan), "EXPLICIT");
        addConstraint(constraints, "inferred_city", plan.getInferredCity(), "QUERY");
        addConstraint(constraints, "inferred_min_price", plan.getInferredMinPrice(), "QUERY");
        addConstraint(constraints, "inferred_max_price", plan.getInferredMaxPrice(), "QUERY");
        return constraints;
    }

    private void addConstraint(List<SearchConstraintResponse> constraints, String key, Object value, String source) {
        if (value == null || value.toString().isBlank()) {
            return;
        }
        constraints.add(SearchConstraintResponse.builder()
                .key(key).value(value.toString()).source(source).build());
    }

    private List<SearchSectionResponse> toSections(
            List<RankedDocument> results,
            Map<UUID, BusinessProfileDto> businessProfiles,
            Map<UUID, List<CatalogImageResponse>> images,
            Map<UUID, List<PurchaseDestinationResponse>> purchaseDestinations,
            String language) {
        List<RankedDocument> exact = results.stream()
                .filter(r -> r.warnings.isEmpty())
                .toList();
        List<RankedDocument> alternatives = results.stream()
                .filter(r -> !r.warnings.isEmpty())
                .toList();
        List<SearchSectionResponse> sections = new ArrayList<>();
        if (!exact.isEmpty()) {
            sections.add(SearchSectionResponse.builder()
                    .type("exact")
                    .kind("EXACT")
                    .title(localized(language, "Совпадения", "Сәйкестіктер", "Matches"))
                    .relaxedConstraints(List.of())
                    .cards(exact.stream().map(r -> toCard(
                            r, businessProfiles, images, purchaseDestinations, language)).toList())
                    .build());
        }
        if (!alternatives.isEmpty()) {
            List<String> relaxed = alternatives.stream()
                    .flatMap(r -> r.warnings.stream())
                    .map(this::constraintForWarning)
                    .distinct()
                    .toList();
            sections.add(SearchSectionResponse.builder()
                    .type("alternatives")
                    .kind("ALTERNATIVE")
                    .title(localized(language, "Альтернативы", "Балама нұсқалар", "Alternatives"))
                    .relaxedConstraints(relaxed)
                    .reason(alternativeReason(relaxed, language))
                    .cards(alternatives.stream().map(r -> toCard(
                            r, businessProfiles, images, purchaseDestinations, language)).toList())
                    .build());
        }
        return sections;
    }

    private String constraintForWarning(String warning) {
        return switch (warning) {
            case "OVER_BUDGET" -> "max_price";
            case "UNDER_BUDGET" -> "min_price";
            case "WRONG_CITY" -> "city";
            default -> warning.toLowerCase(Locale.ROOT);
        };
    }

    private String alternativeReason(List<String> relaxed, String language) {
        if (relaxed.isEmpty()) {
            return localized(language, "Похожие результаты", "Ұқсас нәтижелер", "Related results");
        }
        return localized(language,
                "Дополнительных точных совпадений нет. Ослабленные условия: ",
                "Қосымша дәл сәйкестік табылмады. Жеңілдетілген шарттар: ",
                "No additional exact matches were found; relaxed constraints: ")
                + relaxed.stream()
                        .map(constraint -> localizedConstraint(constraint, language))
                        .collect(Collectors.joining(", "));
    }

    private SearchCardResponse toCard(RankedDocument ranked, Map<UUID, BusinessProfileDto> businessProfiles,
                                       Map<UUID, List<CatalogImageResponse>> images,
                                       Map<UUID, List<PurchaseDestinationResponse>> purchaseDestinations,
                                       String language) {
        SearchDocumentDto doc = ranked.document;
        UUID businessId = doc.getBusinessId();
        BusinessProfileDto brandProfile = businessId == null ? null : businessProfiles.get(businessId);
        return SearchCardResponse.builder()
                .component(component(doc.getDocumentType().name()))
                .resultId(doc.getAggregateId())
                .businessId(doc.getBusinessId())
                .businessName(doc.getBusinessName())
                .resultType(doc.getDocumentType().name())
                .brandColor(resolveBrandColor(brandProfile))
                .brandLogoUrl(brandProfile != null ? brandProfile.getLogoUrl() : null)
                .title(doc.getTitle())
                .summary(doc.getSummary())
                .images(images.getOrDefault(doc.getAggregateId(), List.of()))
                .purchaseDestinations(purchaseDestinations.getOrDefault(doc.getAggregateId(), List.of()))
                .categoryLabel(doc.getCategoryLabel())
                .price(doc.getPrice())
                .currency(doc.getCurrency())
                .businessProfile(toBusinessProfile(brandProfile))
                .availability(doc.getAvailabilityStatus().name())
                .availabilityWarning(doc.getAvailabilityStatus() == SearchAvailabilityStatus.UNKNOWN
                        ? localized(language, "Наличие не подтверждено компанией",
                                "Қолжетімділікті компания растаған жоқ",
                                "Availability has not been confirmed by the business")
                        : null)
                .matchReasons(matchReasons(ranked, language))
                .badges(resolveBadges(brandProfile, doc))
                .distanceMeters(ranked.distanceMeters)
                .latitude(doc.getLatitude())
                .longitude(doc.getLongitude())
                .hasActiveOffer(ranked.activeOfferLabel != null && !ranked.activeOfferLabel.isBlank())
                .branchName(doc.getBranchName())
                .branchAddress(doc.getBranchAddress())
                .branchCity(doc.getCity())
                .build();
    }

    private Map<UUID, List<CatalogImageResponse>> loadImages(List<RankedDocument> results) {
        List<UUID> itemIds = results.stream()
                .filter(result -> result.document.getDocumentType() == SearchDocumentType.ITEM)
                .map(result -> result.document.getAggregateId())
                .toList();
        List<UUID> serviceIds = results.stream()
                .filter(result -> result.document.getDocumentType() == SearchDocumentType.SERVICE)
                .map(result -> result.document.getAggregateId())
                .toList();
        Map<UUID, List<CatalogImageResponse>> images = new HashMap<>();
        if (!itemIds.isEmpty()) {
            productRepository.findByIdIn(itemIds).forEach(item -> images.put(
                    item.getId(), catalogImageMutation.toResponses(item.getImageFiles())));
        }
        if (!serviceIds.isEmpty()) {
            serviceOfferingRepository.findByIdIn(serviceIds).forEach(service -> images.put(
                    service.getId(), catalogImageMutation.toResponses(service.getImageFiles())));
        }
        return images;
    }

    private Map<UUID, List<PurchaseDestinationResponse>> loadPurchaseDestinations(
            List<RankedDocument> results) {
        List<UUID> itemIds = results.stream()
                .filter(result -> result.document.getDocumentType() == SearchDocumentType.ITEM)
                .map(result -> result.document.getAggregateId())
                .toList();
        List<UUID> serviceIds = results.stream()
                .filter(result -> result.document.getDocumentType() == SearchDocumentType.SERVICE)
                .map(result -> result.document.getAggregateId())
                .toList();
        Map<UUID, List<PurchaseDestinationResponse>> destinations = new HashMap<>();
        if (!itemIds.isEmpty()) {
            productRepository.findByIdIn(itemIds).forEach(item -> destinations.put(
                    item.getId(), purchaseDestinationMapper.entitiesToResponses(item.getPurchaseDestinations())));
        }
        if (!serviceIds.isEmpty()) {
            serviceOfferingRepository.findByIdIn(serviceIds).forEach(service -> destinations.put(
                    service.getId(), purchaseDestinationMapper.entitiesToResponses(service.getPurchaseDestinations())));
        }
        return destinations;
    }

    private List<String> matchReasons(RankedDocument ranked, String language) {
        List<String> reasons = new ArrayList<>();
        reasons.add(localized(language, "Соответствует запросу",
                "Сұрауға сәйкес келеді", "Matches the requested product or service"));
        SearchDocumentDto doc = ranked.document;
        if (doc.getCategoryLabel() != null && !doc.getCategoryLabel().isBlank()) {
            reasons.add(localized(language, "Категория: ", "Санат: ", "Category: ") + doc.getCategoryLabel());
        }
        if (ranked.warnings.isEmpty() && doc.getPrice() != null) {
            reasons.add(localized(language, "Соответствует выбранным ценовым условиям",
                    "Таңдалған баға шарттарына сәйкес келеді",
                    "Matches the selected price constraints"));
        }
        return reasons.stream().limit(3).toList();
    }

    private List<String> resolveBadges(BusinessProfileDto profile, SearchDocumentDto doc) {
        List<String> badges = new ArrayList<>();
        if (profile != null && hasOfficialLink(profile)) {
            badges.add("OFFICIAL_CHANNEL");
        }
        if (doc.getSummary() != null && !doc.getSummary().isBlank()) {
            badges.add("COMPLETE_CARD");
        }
        if (doc.getBranchAddress() != null && !doc.getBranchAddress().isBlank()) {
            badges.add("PICKUP");
        }
        return badges;
    }

    private Boolean hasOfficialLink(BusinessProfileDto profile) {
        return (profile.getWebsiteUrl() != null && !profile.getWebsiteUrl().isBlank())
                || (profile.getTelegramUrl() != null && !profile.getTelegramUrl().isBlank())
                || (profile.getInstagramUrl() != null && !profile.getInstagramUrl().isBlank());
    }

    private SearchBusinessProfileResponse toBusinessProfile(BusinessProfileDto profile) {
        if (profile == null) {
            return null;
        }
        return SearchBusinessProfileResponse.builder()
                .logoUrl(profile.getLogoUrl())
                .coverUrl(profile.getCoverUrl())
                .description(profile.getDescription())
                .number(profile.getNumber())
                .email(profile.getEmail())
                .instagramUrl(profile.getInstagramUrl())
                .telegramUrl(profile.getTelegramUrl())
                .websiteUrl(profile.getWebsiteUrl())
                .build();
    }

    private String component(String type) {
        return switch (type) {
            case "ITEM" -> "ItemCard";
            case "SERVICE" -> "ServiceCard";
            default -> throw new InternalServerException(ErrorCode.SEARCH_PROJECTION_INVALID);
        };
    }

    private String resolveBrandColor(BusinessProfileDto profile) {
        if (profile == null || profile.getBrandColor() == null || profile.getBrandColor().isBlank()) {
            return DEFAULT_BRAND_COLOR;
        }
        return profile.getBrandColor();
    }

    private Boolean appliesToBranch(SearchDocumentDto document, UniqueOfferBoostDto boost) {
        if (boost.getBranchIds().isEmpty()) {
            return true;
        }
        return document.getBranchId() != null && boost.getBranchIds().contains(document.getBranchId());
    }

    private String normalizeSort(String sort) {
        if ("distance".equalsIgnoreCase(sort)) {
            return "distance";
        }
        if ("price_asc".equalsIgnoreCase(sort)) {
            return "price_asc";
        }
        if ("price_desc".equalsIgnoreCase(sort)) {
            return "price_desc";
        }
        if ("unique_offers".equalsIgnoreCase(sort)) {
            return "unique_offers";
        }
        return "relevance";
    }

    private List<UUID> activeOfferIds(SearchDocumentType itemType, String sort) {
        if (!"unique_offers".equals(normalizeSort(sort))) {
            return List.of();
        }
        return itemType == SearchDocumentType.ITEM
                ? uniqueOfferService.findAllActiveItemIds()
                : uniqueOfferService.findAllActiveServiceIds();
    }

    private String mapAreaConstraint(SearchPlan plan) {
        if (plan.getMapNorth() == null || plan.getMapSouth() == null
                || plan.getMapEast() == null || plan.getMapWest() == null) {
            return null;
        }
        return plan.getMapNorth() + "," + plan.getMapSouth() + ","
                + plan.getMapEast() + "," + plan.getMapWest();
    }

    private String localized(String language, String russian, String kazakh, String english) {
        String normalizedLanguage = normalize(language);
        if (normalizedLanguage.startsWith("ru")) {
            return russian;
        }
        if (normalizedLanguage.startsWith("kk") || normalizedLanguage.startsWith("kz")) {
            return kazakh;
        }
        return english;
    }

    private String localizedConstraint(String constraint, String language) {
        return switch (constraint) {
            case "city" -> localized(language, "город", "қала", "city");
            case "min_price" -> localized(language, "минимальная цена", "ең төменгі баға", "minimum price");
            case "max_price" -> localized(language, "максимальная цена", "ең жоғары баға", "maximum price");
            default -> constraint;
        };
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    @Getter
    @Builder
    private static class RankedDocument {
        private SearchDocumentDto document;
        private List<String> warnings;
        private Integer distanceMeters;
        private String activeOfferLabel;
    }
}

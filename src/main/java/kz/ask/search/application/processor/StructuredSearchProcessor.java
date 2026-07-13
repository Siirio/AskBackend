package kz.ask.search.application.processor;

import com.fasterxml.jackson.databind.JsonNode;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import kz.ask.business.domain.BrandProfileService;
import kz.ask.business.domain.UniqueOfferService;
import kz.ask.business.domain.dto.BrandProfileDto;
import kz.ask.business.domain.dto.UniqueOfferDto;
import kz.ask.contact.domain.ContactActionService;
import kz.ask.search.api.dto.SearchIntentStructureRequest;
import kz.ask.search.api.dto.SearchLocationRequest;

import kz.ask.search.api.dto.SearchV2CardResponse;
import kz.ask.search.api.dto.SearchV2Request;
import kz.ask.search.api.dto.SearchV2Response;
import kz.ask.search.api.dto.SearchV2SectionResponse;
import kz.ask.search.domain.AttributeKeys;
import kz.ask.search.domain.SearchIntentStructurer;
import kz.ask.search.domain.SearchTermEnricher;
import kz.ask.search.domain.entity.SearchDocument;
import kz.ask.search.domain.enums.SearchDocumentType;
import kz.ask.search.infrastructure.repository.SearchDocumentRepository;
import kz.ask.search.infrastructure.repository.SearchQueryAliasRepository;
import kz.ask.shared.domain.enums.RecordStatus;
import kz.ask.shared.util.DistanceCalculator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class StructuredSearchProcessor {

    private static final Integer MAX_PAGE_SIZE = 50;
    private static final Integer DEFAULT_PAGE_SIZE = 20;
    private static final Integer MINIMUM_RESULT_SCORE = 40;
    private static final Integer HARD_MATCH_SCORE = 50;
    private static final Integer TITLE_MATCH_SCORE = 30;
    private static final Integer BODY_MATCH_SCORE = 25;
    private static final Integer TOKEN_MATCH_SCORE = 20;
    private static final Integer CATEGORY_MATCH_SCORE = 10;
    private static final Integer MUST_HAVE_SCORE = 15;
    private static final Integer NICE_TO_HAVE_SCORE = 5;
    private static final Integer ATTRIBUTE_MATCH_SCORE = 10;
    private static final Integer OVER_BUDGET_PENALTY = 40;
    private static final Integer WRONG_CITY_PENALTY = 30;
    private static final Integer EXACT_SECTION_PRIORITY = 0;
    private static final Integer OVER_BUDGET_SECTION_PRIORITY = 1;
    private static final Integer WRONG_CITY_SECTION_PRIORITY = 2;
    private static final Integer SIMILAR_SECTION_PRIORITY = 3;
    private static final String SECTION_EXACT = "EXACT";
    private static final String SECTION_OVER_BUDGET = "OVER_BUDGET";
    private static final String SECTION_WRONG_CITY = "WRONG_CITY";
    private static final String SECTION_SIMILAR = "SIMILAR";
    private static final String CONFIDENCE_HIGH = "HIGH";
    private static final String CONFIDENCE_MEDIUM = "MEDIUM";
    private static final String CONFIDENCE_LOW = "LOW";
    private static final String DEFAULT_BRAND_COLOR = "#0d9b7c";
    private static final Integer RESULT_LIMIT = 30;
    private static final Pattern MAX_PRICE_PATTERN = Pattern.compile(
            "(?:до|не\\s+дороже|максимум)\\s*(\\d+[\\d\\s]*)(к|k|тыс|тысяч|тг)?");
    private static final Pattern MIN_PRICE_PATTERN = Pattern.compile(
            "(?:от|минимум)\\s*(\\d+[\\d\\s]*)(к|k|тыс|тысяч|тг)?");
    private static final Pattern PRICE_RANGE_PATTERN = Pattern.compile(
            "(\\d+[\\d\\s]*)(к|k|тыс|тысяч|тг)?\\s*-\\s*(\\d+[\\d\\s]*)(к|k|тыс|тысяч|тг)?");
    private static final Pattern PACKAGE_VALUE_PATTERN = Pattern.compile(
            "(\\d+[\\d\\s.,]*)(?:\\s*)(кг|kg|килограмм|килограмма|килограммов|г|гр|g|gram|грамм|грамма|граммов)");
    private static final Pattern MIN_PACKAGE_PATTERN = Pattern.compile(
            "(?:>|от|больше|свыше|не\\s+меньше)\\s*(\\d+[\\d\\s.,]*)(?:\\s*)(кг|kg|килограмм|килограмма|килограммов|г|гр|g|gram|грамм|грамма|граммов)");
    private static final Pattern MAX_PACKAGE_PATTERN = Pattern.compile(
            "(?:<|до|меньше|не\\s+больше)\\s*(\\d+[\\d\\s.,]*)(?:\\s*)(кг|kg|килограмм|килограмма|килограммов|г|гр|g|gram|грамм|грамма|граммов)");

    private final SearchIntentStructurer searchIntentStructurer;
    private final SearchDocumentRepository searchDocumentRepository;
    private final SearchQueryAliasRepository searchQueryAliasRepository;
    private final IntentCategoryMapper intentCategoryMapper;
    private final SearchTermEnricher searchTermEnricher;
    private final BrandProfileService brandProfileService;
    private final UniqueOfferService uniqueOfferService;
    private final ContactActionService contactActionService;

    @Transactional(readOnly = true)
    public SearchV2Response search(SearchV2Request request) {
        SearchIntentStructureRequest aiRequest = toIntentRequest(request);
        JsonNode intentStructure = searchIntentStructurer.structure(aiRequest);
        SearchPlan searchPlan = buildSearchPlan(intentStructure, aiRequest);
        SearchLocationRequest userLocation = request.getUserLocation();

        List<ScoredSearchDocument> scored = rank(
                searchDocumentRepository.findActiveCandidates(resolvePlanDocumentTypes(searchPlan)),
                searchPlan, userLocation);

        List<SearchV2CardResponse> cards = scored.stream()
                .limit(RESULT_LIMIT)
                .map(this::toV2Card)
                .toList();

        return SearchV2Response.builder()
                .searchSessionId(null)
                .rawQuery(request.getRawQuery())
                .scope(resolveScope(searchPlan))
                .understoodQuery(request.getRawQuery())
                .sections(groupSections(cards))
                .supplierCheckCount(0)
                .build();
    }

    private SearchIntentStructureRequest toIntentRequest(SearchV2Request request) {
        SearchIntentStructureRequest aiRequest = new SearchIntentStructureRequest();
        aiRequest.setRawQuery(request.getRawQuery());
        aiRequest.setSelectedMode(resolveMode(request.getScope()));
        aiRequest.setSelectedCategory(request.getSelectedCategory());
        aiRequest.setCity(request.getCity());
        aiRequest.setUserLocation(request.getUserLocation());
        aiRequest.setLanguage(request.getLanguage());
        return aiRequest;
    }

    private String resolveMode(String scope) {
        if ("product".equalsIgnoreCase(scope)) {
            return "PRODUCT";
        }
        if ("service".equalsIgnoreCase(scope)) {
            return "SERVICE";
        }
        return "AUTO";
    }

    private String resolveScope(SearchPlan plan) {
        if (plan.getItemType() == SearchDocumentType.PRODUCT) {
            return "PRODUCT";
        }
        if (plan.getItemType() == SearchDocumentType.SERVICE) {
            return "SERVICE";
        }
        return "ALL";
    }

    SearchPlan buildSearchPlan(JsonNode intentStructure, SearchIntentStructureRequest request) {
        List<String> categoryInputs = collectCategoryInputs(intentStructure, request);
        List<StructuredCategorySignal> categorySignals = intentCategoryMapper.map(categoryInputs);
        List<String> categoryAliases = collectCategoryAliases(categorySignals, request);
        List<String> exactTerms = resolveExactTerms(intentStructure, request);
        List<String> semanticTerms = resolveSemanticTerms(intentStructure);
        List<String> relatedTerms = combineTerms(resolveRelatedTerms(intentStructure), resolveAliasTargets(exactTerms),
                resolveAliasTargets(categoryAliases), resolveAliasTargets(semanticTerms));
        return SearchPlan.builder()
                .itemType(resolveStructuredItemType(intentStructure))
                .city(normalize(request.getCity()))
                .userSelectedCategory(normalize(request.getSelectedCategory()))
                .minPrice(resolveMinPrice(intentStructure, request))
                .maxPrice(resolveMaxPrice(intentStructure, request))
                .minPackageGrams(resolveMinPackageGrams(request))
                .maxPackageGrams(resolveMaxPackageGrams(request))
                .canonicalCategoryKeys(categorySignals.stream().map(StructuredCategorySignal::getCanonicalKey).toList())
                .categoryAliases(categoryAliases)
                .hardMatchTerms(resolveHardMatchTerms(intentStructure, request))
                .qualifierTerms(resolveQualifierTerms(intentStructure, request))
                .exactTerms(exactTerms)
                .semanticTerms(semanticTerms)
                .synonyms(resolveArray(intentStructure.path("semantic").path("synonyms")))
                .relatedTerms(relatedTerms)
                .mustHave(resolveArray(intentStructure.path("constraints").path("must_have")))
                .niceToHave(resolveArray(intentStructure.path("constraints").path("nice_to_have")))
                .notWanted(resolveArray(intentStructure.path("constraints").path("not_wanted")))
                .rankingPriorities(resolveArray(intentStructure.path("ranking").path("prioritize")))
                .intentAttributes(extractIntentAttributes(intentStructure))
                .build();
    }

    private List<ScoredSearchDocument> rank(List<SearchDocument> candidates, SearchPlan plan,
                                            SearchLocationRequest userLocation) {
        return candidates.stream()
                .map(document -> score(document, plan, userLocation))
                .filter(scored -> scored.getScore() >= MINIMUM_RESULT_SCORE)
                .sorted(Comparator.comparing(this::sectionPriority)
                        .thenComparing(ScoredSearchDocument::getScore, Comparator.reverseOrder()))
                .toList();
    }

    private ScoredSearchDocument score(SearchDocument document, SearchPlan plan,
                                       SearchLocationRequest userLocation) {
        if (!passesHardSemanticGate(document, plan)) {
            return rejected(document);
        }
        List<String> warnings = new ArrayList<>();
        Integer score = HARD_MATCH_SCORE;
        score += scoreTerms(document, plan.getHardMatchTerms(), TITLE_MATCH_SCORE, TOKEN_MATCH_SCORE, BODY_MATCH_SCORE);
        score += scoreTerms(document, plan.getQualifierTerms(), TITLE_MATCH_SCORE, TOKEN_MATCH_SCORE, BODY_MATCH_SCORE);
        score += scoreTerms(document, plan.getExactTerms(), TITLE_MATCH_SCORE, TOKEN_MATCH_SCORE, BODY_MATCH_SCORE);
        score += scoreTerms(document, plan.getMustHave(), MUST_HAVE_SCORE, MUST_HAVE_SCORE, MUST_HAVE_SCORE);
        score += scoreTerms(document, plan.getCategoryAliases(), CATEGORY_MATCH_SCORE, CATEGORY_MATCH_SCORE, CATEGORY_MATCH_SCORE);
        score += scoreTerms(document, plan.getSemanticTerms(), TITLE_MATCH_SCORE, TOKEN_MATCH_SCORE, BODY_MATCH_SCORE);
        score += scoreTerms(document, plan.getSynonyms(), CATEGORY_MATCH_SCORE, CATEGORY_MATCH_SCORE, NICE_TO_HAVE_SCORE);
        score += scoreTerms(document, plan.getNiceToHave(), NICE_TO_HAVE_SCORE, NICE_TO_HAVE_SCORE, NICE_TO_HAVE_SCORE);
        score += scoreStructuredAttributes(document, plan);
        score = score - pricePenalty(document, plan, warnings) - cityPenalty(document, plan, warnings);
        Integer distanceMeters = null;
        String distanceText = null;
        if (userLocation != null && document.getBranch() != null
                && document.getBranch().getLatitude() != null
                && document.getBranch().getLongitude() != null) {
            double km = DistanceCalculator.km(
                    userLocation.getLat(), userLocation.getLng(),
                    document.getBranch().getLatitude().doubleValue(),
                    document.getBranch().getLongitude().doubleValue());
            distanceMeters = DistanceCalculator.meters(
                    userLocation.getLat(), userLocation.getLng(),
                    document.getBranch().getLatitude().doubleValue(),
                    document.getBranch().getLongitude().doubleValue());
            distanceText = formatDistance(distanceMeters);
            score = (int) (score * Math.max(0.3, 1.0 / (1.0 + km * 0.5)));
        }
        return ScoredSearchDocument.builder()
                .document(document)
                .score(score)
                .sectionType(resolveSectionType(warnings, score))
                .confidenceCode(resolveConfidenceCode(score, warnings))
                .warnings(warnings)
                .distanceMeters(distanceMeters)
                .distanceText(distanceText)
                .build();
    }

    private ScoredSearchDocument rejected(SearchDocument document) {
        return ScoredSearchDocument.builder()
                .document(document)
                .score(0)
                .sectionType(SECTION_SIMILAR)
                .confidenceCode(CONFIDENCE_LOW)
                .warnings(List.of())
                .build();
    }

    private Boolean passesHardSemanticGate(SearchDocument document, SearchPlan plan) {
        return !matchesNotWanted(document, plan)
                && matchesHardTerms(document, plan)
                && matchesPackageConstraint(document, plan)
                && matchesQualifierTerms(document, plan)
                && matchesRequiredFeatureTerms(document, plan)
                && matchesStructuredAttributes(document, plan);
    }

    private Boolean matchesHardTerms(SearchDocument document, SearchPlan plan) {
        if (plan.getHardMatchTerms().isEmpty()) {
            return true;
        }
        String text = documentText(document);
        return plan.getHardMatchTerms().stream().anyMatch(term -> contains(text, normalize(term)));
    }

    private Boolean matchesQualifierTerms(SearchDocument document, SearchPlan plan) {
        String text = documentText(document);
        return plan.getQualifierTerms().stream().allMatch(term -> contains(text, normalize(term)));
    }

    private Boolean matchesRequiredFeatureTerms(SearchDocument document, SearchPlan plan) {
        List<String> featureTerms = plan.getMustHave().stream().map(this::normalize)
                .filter(term -> !term.isBlank() && term.split("\\s+").length > 1)
                .filter(term -> !isPackageConstraintTerm(term))
                .toList();
        if (featureTerms.isEmpty()) {
            return true;
        }
        String text = documentText(document);
        return featureTerms.stream().allMatch(term -> contains(text, term));
    }

    private Boolean matchesPackageConstraint(SearchDocument document, SearchPlan plan) {
        if (plan.getMinPackageGrams() == null && plan.getMaxPackageGrams() == null) {
            return true;
        }
        List<BigDecimal> values = parsePackageGrams(documentTextWithTokens(document));
        if (values.isEmpty()) {
            return false;
        }
        return values.stream().anyMatch(value -> matchesPackageValue(value, plan));
    }

    private Boolean matchesPackageValue(BigDecimal value, SearchPlan plan) {
        if (plan.getMinPackageGrams() != null && value.compareTo(plan.getMinPackageGrams()) < 0) {
            return false;
        }
        return plan.getMaxPackageGrams() == null || value.compareTo(plan.getMaxPackageGrams()) <= 0;
    }

    private Integer scoreTerms(SearchDocument document, List<String> terms, Integer titleScore,
                               Integer tokenScore, Integer bodyScore) {
        Integer score = 0;
        for (String term : terms) {
            score += scoreTerm(document, normalize(term), titleScore, tokenScore, bodyScore);
        }
        return score;
    }

    private Integer scoreTerm(SearchDocument document, String term, Integer titleScore,
                              Integer tokenScore, Integer bodyScore) {
        if (term.isBlank()) {
            return 0;
        }
        Integer score = contains(normalize(document.getTitle()), term) ? titleScore : 0;
        score += tokenContains(document, term) ? tokenScore : 0;
        score += contains(documentText(document), term) ? bodyScore : 0;
        return score;
    }

    private Boolean matchesNotWanted(SearchDocument document, SearchPlan plan) {
        String text = documentText(document);
        return plan.getNotWanted().stream().map(this::normalize).anyMatch(term -> !term.isBlank() && contains(text, term));
    }

    private Integer pricePenalty(SearchDocument document, SearchPlan plan, List<String> warnings) {
        if (document.getPrice() == null) {
            return 0;
        }
        if (plan.getMaxPrice() != null && document.getPrice().compareTo(plan.getMaxPrice()) > 0) {
            warnings.add("OVER_BUDGET");
            return OVER_BUDGET_PENALTY;
        }
        if (plan.getMinPrice() != null && document.getPrice().compareTo(plan.getMinPrice()) < 0) {
            warnings.add("UNDER_BUDGET");
            return OVER_BUDGET_PENALTY;
        }
        return 0;
    }

    private Integer cityPenalty(SearchDocument document, SearchPlan plan, List<String> warnings) {
        if (plan.getCity().isBlank() || document.getBranch() == null || document.getBranch().getCity() == null) {
            return 0;
        }
        if (contains(normalize(document.getBranch().getCity().getName()), plan.getCity())) {
            return 0;
        }
        warnings.add("WRONG_CITY");
        return WRONG_CITY_PENALTY;
    }

    private String resolveSectionType(List<String> warnings, Integer score) {
        if (warnings.contains("OVER_BUDGET") || warnings.contains("UNDER_BUDGET")) {
            return SECTION_OVER_BUDGET;
        }
        if (warnings.contains("WRONG_CITY")) {
            return SECTION_WRONG_CITY;
        }
        if (score >= MINIMUM_RESULT_SCORE) {
            return SECTION_EXACT;
        }
        return SECTION_SIMILAR;
    }

    private String resolveConfidenceCode(Integer score, List<String> warnings) {
        if (!warnings.isEmpty()) {
            return CONFIDENCE_LOW;
        }
        if (score >= 70) {
            return CONFIDENCE_HIGH;
        }
        return CONFIDENCE_MEDIUM;
    }

    private Integer sectionPriority(ScoredSearchDocument scored) {
        if (SECTION_EXACT.equals(scored.getSectionType())) {
            return EXACT_SECTION_PRIORITY;
        }
        if (SECTION_OVER_BUDGET.equals(scored.getSectionType())) {
            return OVER_BUDGET_SECTION_PRIORITY;
        }
        if (SECTION_WRONG_CITY.equals(scored.getSectionType())) {
            return WRONG_CITY_SECTION_PRIORITY;
        }
        return SIMILAR_SECTION_PRIORITY;
    }

    private Boolean tokenContains(SearchDocument document, String term) {
        return document.getTokens().stream().map(this::normalize).anyMatch(token -> contains(token, term));
    }

    private String documentText(SearchDocument document) {
        return String.join(" ",
                normalize(document.getTitle()),
                normalize(document.getSummary()),
                normalize(document.getCategoryLabel()),
                normalize(document.getSku()),
                normalize(document.getCharacteristicsJson()),
                document.getBusiness() != null ? normalize(document.getBusiness().getName()) : "",
                document.getBranch() != null ? normalize(document.getBranch().getName()) : "");
    }

    private String documentTextWithTokens(SearchDocument document) {
        return documentText(document) + " " + String.join(" ", document.getTokens().stream().map(this::normalize).toList());
    }

    private Boolean contains(String value, String term) {
        if (value.isBlank() || term.isBlank()) {
            return false;
        }
        return value.contains(term);
    }

    private SearchDocumentType resolveStructuredItemType(JsonNode intentStructure) {
        String requestType = intentStructure.path("request_type").asText("");
        if ("PRODUCT_SEARCH".equalsIgnoreCase(requestType)) {
            return SearchDocumentType.PRODUCT;
        }
        if ("SERVICE_SEARCH".equalsIgnoreCase(requestType)) {
            return SearchDocumentType.SERVICE;
        }
        return null;
    }

    private List<SearchDocumentType> resolvePlanDocumentTypes(SearchPlan plan) {
        if (plan.getItemType() != null) {
            return List.of(plan.getItemType());
        }
        return List.of(SearchDocumentType.PRODUCT, SearchDocumentType.SERVICE);
    }

    private List<String> collectCategoryInputs(JsonNode intentStructure, SearchIntentStructureRequest request) {
        Set<String> terms = new LinkedHashSet<>();
        addTerm(terms, request.getSelectedCategory());
        addTerm(terms, intentStructure.path("product").path("primary_category").asText(""));
        addTerm(terms, intentStructure.path("product").path("subcategory").asText(""));
        addTerm(terms, intentStructure.path("product").path("product_type").asText(""));
        addTerm(terms, intentStructure.path("service").path("primary_category").asText(""));
        addTerm(terms, intentStructure.path("service").path("subcategory").asText(""));
        addTerm(terms, intentStructure.path("service").path("service_type").asText(""));
        return terms.stream().toList();
    }

    private List<String> collectCategoryAliases(List<StructuredCategorySignal> signals, SearchIntentStructureRequest request) {
        Set<String> terms = new LinkedHashSet<>();
        addTerm(terms, request.getSelectedCategory());
        signals.forEach(signal -> addTerms(terms, signal.getAliases()));
        return terms.stream().toList();
    }

    private List<String> resolveHardMatchTerms(JsonNode intentStructure, SearchIntentStructureRequest request) {
        Set<String> terms = new LinkedHashSet<>();
        String sourceText = normalize(String.join(" ", normalize(request.getRawQuery()), resolveSemanticTerms(intentStructure).toString(),
                intentStructure.path("product").path("product_type").asText(""),
                intentStructure.path("service").path("service_type").asText("")));
        addKnownIntentTerms(terms, sourceText);
        addConcreteIntentTerm(terms, intentStructure.path("product").path("product_type").asText(""));
        addConcreteIntentTerm(terms, intentStructure.path("service").path("service_type").asText(""));
        addFeaturePhrase(terms, request.getRawQuery());
        return terms.stream().toList();
    }

    private void addKnownIntentTerms(Set<String> terms, String sourceText) {
        if (containsAny(sourceText, List.of("смартфон", "smartphone", "iphone", "айфон", "galaxy"))) {
            addTerms(terms, List.of("смартфон", "smartphone", "iphone", "galaxy"));
        }
        if (containsAny(sourceText, List.of("ноутбук", "laptop", "macbook"))) {
            addTerms(terms, List.of("ноутбук", "laptop", "macbook"));
        }
        if (containsAny(sourceText, List.of("пылесос", "vacuum", "dyson"))) {
            addTerms(terms, List.of("пылесос", "vacuum", "dyson"));
        }
        if (containsAny(sourceText, List.of("наушник", "headphone", "sony wh"))) {
            addTerms(terms, List.of("наушник", "headphone"));
        }
        if (containsAny(sourceText, List.of("маникюр", "manicure", "ногти", "гель-лак"))) {
            addTerms(terms, List.of("маникюр", "manicure", "ногти", "гель-лак"));
        }
        if (containsAny(sourceText, List.of("стрижка", "haircut", "подстричь"))) {
            addTerms(terms, List.of("стрижка", "haircut"));
        }
        if (containsAny(sourceText, List.of("окрашивание", "coloring", "цвет волос"))) {
            addTerms(terms, List.of("окрашивание", "coloring"));
        }
        addTerms(terms, searchTermEnricher.expandIntentTerms(sourceText));
    }

    private void addConcreteIntentTerm(Set<String> terms, String value) {
        String normalized = normalize(value);
        if (normalized.isBlank() || isBroadIntentTerm(normalized)) {
            return;
        }
        addTerm(terms, normalized);
    }

    private Boolean isBroadIntentTerm(String value) {
        return List.of("product", "service", "electronics", "beauty_services", "услуги красоты",
                "бытовая техника", "товар", "услуга").contains(value);
    }

    private void addFeaturePhrase(Set<String> terms, String rawQuery) {
        String normalized = normalize(rawQuery);
        if (!containsKnownCommercialType(normalized) && normalized.split("\\s+").length > 1) {
            addTerm(terms, removePriceWording(normalized));
        }
    }

    private Boolean containsKnownCommercialType(String value) {
        return searchTermEnricher.isKnownCommercialType(value)
                || containsAny(value, List.of("смартфон", "smartphone", "iphone", "айфон", "galaxy", "ноутбук",
                "laptop", "macbook", "пылесос", "vacuum", "dyson", "наушник", "headphone",
                "маникюр", "manicure", "стрижка", "haircut", "окрашивание"));
    }

    private List<String> resolveQualifierTerms(JsonNode intentStructure, SearchIntentStructureRequest request) {
        Set<String> terms = new LinkedHashSet<>();
        String sourceText = normalize(String.join(" ", normalize(request.getRawQuery()),
                intentStructure.path("service").path("target_customer").asText("")));
        if (containsAny(sourceText, List.of("женская", "женский", "женск", "female"))) {
            addTerm(terms, "женск");
        }
        if (containsAny(sourceText, List.of("мужская", "мужской", "мужск", "male"))) {
            addTerm(terms, "мужск");
        }
        if (containsAny(sourceText, List.of("борода", "beard"))) {
            addTerm(terms, "бород");
        }
        return terms.stream().toList();
    }

    private List<String> resolveExactTerms(JsonNode intentStructure, SearchIntentStructureRequest request) {
        Set<String> terms = new LinkedHashSet<>();
        addTerm(terms, request.getRawQuery());
        addTerm(terms, intentStructure.path("product").path("normalized_product_name").asText(""));
        addTerm(terms, intentStructure.path("product").path("product_type").asText(""));
        addTerm(terms, intentStructure.path("service").path("service_type").asText(""));
        addArrayTerms(terms, intentStructure.path("semantic").path("search_keywords"));
        return terms.stream().toList();
    }

    private List<String> resolveSemanticTerms(JsonNode intentStructure) {
        Set<String> terms = new LinkedHashSet<>();
        addTerm(terms, intentStructure.path("semantic").path("semantic_query").asText(""));
        addTerm(terms, intentStructure.path("service").path("desired_result").asText(""));
        addTerm(terms, intentStructure.path("fallback_search").path("semantic_query").asText(""));
        return terms.stream().toList();
    }

    private List<String> resolveRelatedTerms(JsonNode intentStructure) {
        Set<String> terms = new LinkedHashSet<>();
        addArrayTerms(terms, intentStructure.path("semantic").path("related_terms"));
        addArrayTerms(terms, intentStructure.path("ranking").path("expand_if_no_results"));
        return terms.stream().toList();
    }

    private BigDecimal resolveMinPrice(JsonNode intentStructure, SearchIntentStructureRequest request) {
        BigDecimal price = resolveJsonPrice(intentStructure.path("price").path("min"));
        if (price != null) {
            return price;
        }
        return parsePrice(request.getRawQuery(), MIN_PRICE_PATTERN, true);
    }

    private BigDecimal resolveMaxPrice(JsonNode intentStructure, SearchIntentStructureRequest request) {
        BigDecimal price = resolveJsonPrice(intentStructure.path("price").path("max"));
        if (price != null) {
            return price;
        }
        BigDecimal rangeMax = parseRangeMaxPrice(request.getRawQuery());
        if (rangeMax != null) {
            return rangeMax;
        }
        return parsePrice(request.getRawQuery(), MAX_PRICE_PATTERN, false);
    }

    private BigDecimal resolveMinPackageGrams(SearchIntentStructureRequest request) {
        return parsePackageConstraint(request.getRawQuery(), MIN_PACKAGE_PATTERN);
    }

    private BigDecimal resolveMaxPackageGrams(SearchIntentStructureRequest request) {
        return parsePackageConstraint(request.getRawQuery(), MAX_PACKAGE_PATTERN);
    }

    private BigDecimal resolveJsonPrice(JsonNode node) {
        if (node == null || node.isMissingNode() || node.isNull() || !node.isNumber()) {
            return null;
        }
        return node.decimalValue();
    }

    private BigDecimal parseRangeMaxPrice(String query) {
        Matcher matcher = PRICE_RANGE_PATTERN.matcher(normalize(query));
        if (!matcher.find()) {
            return null;
        }
        return normalizePrice(matcher.group(3), matcher.group(4));
    }

    private BigDecimal parsePrice(String query, Pattern pattern, Boolean firstRangeValue) {
        Matcher rangeMatcher = PRICE_RANGE_PATTERN.matcher(normalize(query));
        if (rangeMatcher.find() && firstRangeValue) {
            return normalizePrice(rangeMatcher.group(1), rangeMatcher.group(2));
        }
        Matcher matcher = pattern.matcher(normalize(query));
        if (!matcher.find()) {
            return null;
        }
        return normalizePrice(matcher.group(1), matcher.group(2));
    }

    private BigDecimal normalizePrice(String rawNumber, String rawSuffix) {
        BigDecimal value = new BigDecimal(rawNumber.replace(" ", ""));
        String suffix = normalize(rawSuffix);
        if (List.of("к", "k", "тыс", "тысяч").contains(suffix)) {
            return value.multiply(BigDecimal.valueOf(1000L));
        }
        return value;
    }

    private BigDecimal parsePackageConstraint(String query, Pattern pattern) {
        Matcher matcher = pattern.matcher(normalize(query));
        if (!matcher.find()) {
            return null;
        }
        return normalizePackageGrams(matcher.group(1), matcher.group(2));
    }

    private List<BigDecimal> parsePackageGrams(String text) {
        List<BigDecimal> values = new ArrayList<>();
        Matcher matcher = PACKAGE_VALUE_PATTERN.matcher(normalize(text));
        while (matcher.find()) {
            values.add(normalizePackageGrams(matcher.group(1), matcher.group(2)));
        }
        return values;
    }

    private BigDecimal normalizePackageGrams(String rawNumber, String rawUnit) {
        BigDecimal value = new BigDecimal(rawNumber.replace(" ", "").replace(",", "."));
        String unit = normalize(rawUnit);
        if (List.of("кг", "kg", "килограмм", "килограмма", "килограммов").contains(unit)) {
            return value.multiply(BigDecimal.valueOf(1000L));
        }
        return value;
    }

    private Boolean isPackageConstraintTerm(String term) {
        return PACKAGE_VALUE_PATTERN.matcher(normalize(term)).find();
    }

    private String removePriceWording(String value) {
        return MAX_PRICE_PATTERN.matcher(MIN_PRICE_PATTERN.matcher(PRICE_RANGE_PATTERN.matcher(value)
                .replaceAll("")).replaceAll("")).replaceAll("").trim();
    }

    private Boolean containsAny(String value, List<String> terms) {
        return terms.stream().map(this::normalize).anyMatch(term -> !term.isBlank() && value.contains(term));
    }

    private List<String> resolveAliasTargets(List<String> sourceTerms) {
        Set<String> terms = new LinkedHashSet<>();
        for (String sourceTerm : sourceTerms) {
            String normalized = normalize(sourceTerm);
            if (!normalized.isBlank()) {
                searchQueryAliasRepository.findByAliasValueAndStatus(normalized, RecordStatus.ACTIVE)
                        .stream()
                        .map(alias -> normalize(alias.getTargetQuery()))
                        .forEach(target -> addTerm(terms, target));
            }
        }
        return terms.stream().toList();
    }

    @SafeVarargs
    private final List<String> combineTerms(List<String>... termGroups) {
        Set<String> terms = new LinkedHashSet<>();
        for (List<String> termGroup : termGroups) {
            addTerms(terms, termGroup);
        }
        return terms.stream().toList();
    }

    private List<String> resolveArray(JsonNode node) {
        Set<String> terms = new LinkedHashSet<>();
        addArrayTerms(terms, node);
        return terms.stream().toList();
    }

    private void addArrayTerms(Set<String> queryTerms, JsonNode node) {
        if (node.isArray()) {
            node.forEach(item -> addTerm(queryTerms, item.asText("")));
        }
    }

    private void addTerms(Set<String> queryTerms, List<String> terms) {
        terms.forEach(term -> addTerm(queryTerms, term));
    }

    private void addTerm(Set<String> queryTerms, String value) {
        String normalized = normalize(value);
        if (!normalized.isBlank()) {
            queryTerms.add(normalized);
        }
    }

    private void addTerm(List<String> queryTerms, String value) {
        String normalized = normalize(value);
        if (!normalized.isBlank() && !queryTerms.contains(normalized)) {
            queryTerms.add(normalized);
        }
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    private SearchV2CardResponse toV2Card(ScoredSearchDocument scored) {
        SearchDocument document = scored.getDocument();
        BrandProfileDto brandProfile = resolveBrandProfile(document);
        List<UniqueOfferDto> offers = resolveOffers(document);
        return SearchV2CardResponse.builder()
                .component(component(document.getDocumentType().name()))
                .resultId(document.getId())
                .businessId(document.getBusiness() != null ? document.getBusiness().getId() : null)
                .businessName(document.getBusiness() != null ? document.getBusiness().getName() : null)
                .brandColor(resolveBrandColor(brandProfile))
                .brandLogoUrl(brandProfile != null ? brandProfile.getLogoUrl() : null)
                .title(document.getTitle())
                .price(document.getPrice())
                .availability("NEEDS_CONFIRMATION")
                .badges(resolveBadges(brandProfile, document, offers))
                .distanceMeters(scored.getDistanceMeters())
                .branchName(document.getBranch() != null ? document.getBranch().getName() : null)
                .hasActiveDrop(!offers.isEmpty())
                .contactActions(document.getBusiness() != null
                        ? contactActionService.summarize(document.getBusiness().getId())
                        : List.of())
                .build();
    }

    private BrandProfileDto resolveBrandProfile(SearchDocument document) {
        if (document.getBusiness() == null) {
            return null;
        }
        return brandProfileService.findByBusinessId(document.getBusiness().getId());
    }

    private List<UniqueOfferDto> resolveOffers(SearchDocument document) {
        if (document.getBusiness() == null) {
            return List.of();
        }
        return uniqueOfferService.listPublic(document.getBusiness().getId());
    }

    private String resolveBrandColor(BrandProfileDto profile) {
        if (profile == null || profile.getBrandColor() == null || profile.getBrandColor().isBlank()) {
            return DEFAULT_BRAND_COLOR;
        }
        return profile.getBrandColor();
    }

    private List<String> resolveBadges(BrandProfileDto profile, SearchDocument document, List<UniqueOfferDto> offers) {
        List<String> badges = new ArrayList<>();
        if (profile != null && hasOfficialLink(profile)) {
            badges.add("official channel");
        }
        if (document.getSummary() != null && !document.getSummary().isBlank()) {
            badges.add("complete card");
        }
        if (document.getBranch() != null && document.getBranch().getAddress() != null && !document.getBranch().getAddress().isBlank()) {
            badges.add("pickup");
        }
        if (!offers.isEmpty()) {
            badges.add("active drop");
        }
        return badges;
    }

    private Boolean hasOfficialLink(BrandProfileDto profile) {
        return (profile.getWebsiteUrl() != null && !profile.getWebsiteUrl().isBlank())
                || (profile.getTelegramUrl() != null && !profile.getTelegramUrl().isBlank())
                || (profile.getInstagramUrl() != null && !profile.getInstagramUrl().isBlank());
    }

    private String component(String type) {
        if ("SERVICE".equals(type)) {
            return "ServiceCard";
        }
        return "ProductCard";
    }

    private List<SearchV2SectionResponse> groupSections(List<SearchV2CardResponse> cards) {
        Map<String, List<SearchV2CardResponse>> sections = new LinkedHashMap<>();
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
        if (card.getPrice() == null && !"ServiceCard".equals(card.getComponent())) {
            return "needs_confirmation";
        }
        return "exact_products";
    }

    private String sectionTitle(String sectionType) {
        return switch (sectionType) {
            case "over_budget" -> "Похожие варианты дороже бюджета";
            case "needs_confirmation" -> "Нужно уточнить у поставщика";
            case "similar_products" -> "Похожие варианты";
            default -> "Подходит под запрос";
        };
    }

    private String formatDistance(Integer meters) {
        if (meters == null) {
            return null;
        }
        if (meters < 1000) {
            return meters + " м";
        }
        double km = meters / 1000.0;
        return String.format(Locale.ROOT, "%.1f км", km);
    }

    private Map<String, Object> extractIntentAttributes(JsonNode intentStructure) {
        Map<String, Object> attrs = new HashMap<>();
        String requestType = intentStructure.path("request_type").asText("");
        JsonNode attrNode;
        if ("PRODUCT_SEARCH".equals(requestType)) {
            attrNode = intentStructure.path("product").path("attributes");
        } else if ("SERVICE_SEARCH".equals(requestType)) {
            attrNode = intentStructure.path("service").path("attributes");
        } else {
            return attrs;
        }
        for (String key : AttributeKeys.ALL_KEYS) {
            JsonNode valueNode = attrNode.path(key);
            if (valueNode.isMissingNode() || valueNode.isNull()) {
                continue;
            }
            if (valueNode.isArray() && !valueNode.isEmpty()) {
                List<String> values = new ArrayList<>();
                valueNode.forEach(v -> {
                    String text = normalize(v.asText(""));
                    if (!text.isBlank()) {
                        values.add(text);
                    }
                });
                if (!values.isEmpty()) {
                    attrs.put(key, values);
                }
            } else if (valueNode.isTextual()) {
                String text = normalize(valueNode.asText(""));
                if (!text.isBlank()) {
                    attrs.put(key, text);
                }
            }
        }
        return attrs;
    }

    private Boolean matchesStructuredAttributes(SearchDocument document, SearchPlan plan) {
        Map<String, Object> intentAttrs = plan.getIntentAttributes();
        if (intentAttrs == null || intentAttrs.isEmpty()) {
            return true;
        }
        Map<String, Object> docAttrs = document.getAttributes();
        if (docAttrs == null || docAttrs.isEmpty()) {
            return true;
        }
        if (intentAttrs.containsKey(AttributeKeys.BRAND) && docAttrs.containsKey(AttributeKeys.BRAND)
                && !attributeValuesMatch(intentAttrs.get(AttributeKeys.BRAND), docAttrs.get(AttributeKeys.BRAND))) {
            return false;
        }
        if (intentAttrs.containsKey(AttributeKeys.SIZE) && docAttrs.containsKey(AttributeKeys.SIZE)
                && !attributeValuesMatch(intentAttrs.get(AttributeKeys.SIZE), docAttrs.get(AttributeKeys.SIZE))) {
            return false;
        }
        if (intentAttrs.containsKey(AttributeKeys.AUDIENCE) && docAttrs.containsKey(AttributeKeys.AUDIENCE)
                && !attributeValuesMatch(intentAttrs.get(AttributeKeys.AUDIENCE), docAttrs.get(AttributeKeys.AUDIENCE))) {
            return false;
        }
        if (intentAttrs.containsKey(AttributeKeys.CONDITION) && docAttrs.containsKey(AttributeKeys.CONDITION)
                && !attributeValuesMatch(intentAttrs.get(AttributeKeys.CONDITION), docAttrs.get(AttributeKeys.CONDITION))) {
            return false;
        }
        return true;
    }

    private Integer scoreStructuredAttributes(SearchDocument document, SearchPlan plan) {
        Map<String, Object> intentAttrs = plan.getIntentAttributes();
        if (intentAttrs == null || intentAttrs.isEmpty()) {
            return 0;
        }
        Map<String, Object> docAttrs = document.getAttributes();
        if (docAttrs == null || docAttrs.isEmpty()) {
            return 0;
        }
        Integer score = 0;
        if (intentAttrs.containsKey(AttributeKeys.COLOR) && docAttrs.containsKey(AttributeKeys.COLOR)
                && attributeValuesMatch(intentAttrs.get(AttributeKeys.COLOR), docAttrs.get(AttributeKeys.COLOR))) {
            score += ATTRIBUTE_MATCH_SCORE;
        }
        if (intentAttrs.containsKey(AttributeKeys.MATERIAL) && docAttrs.containsKey(AttributeKeys.MATERIAL)
                && attributeValuesMatch(intentAttrs.get(AttributeKeys.MATERIAL), docAttrs.get(AttributeKeys.MATERIAL))) {
            score += ATTRIBUTE_MATCH_SCORE;
        }
        if (intentAttrs.containsKey(AttributeKeys.OCCASION) && docAttrs.containsKey(AttributeKeys.OCCASION)
                && attributeValuesMatch(intentAttrs.get(AttributeKeys.OCCASION), docAttrs.get(AttributeKeys.OCCASION))) {
            score += ATTRIBUTE_MATCH_SCORE;
        }
        return score;
    }

    @SuppressWarnings("unchecked")
    private Boolean attributeValuesMatch(Object intentValue, Object docValue) {
        List<String> intentItems = toNormalizedList(intentValue);
        List<String> docItems = toNormalizedList(docValue);
        if (intentItems.isEmpty() || docItems.isEmpty()) {
            return false;
        }
        return intentItems.stream().anyMatch(iv -> docItems.stream().anyMatch(dv -> dv.equals(iv)));
    }

    private List<String> toNormalizedList(Object value) {
        if (value instanceof List) {
            List<String> result = new ArrayList<>();
            for (Object item : (List<?>) value) {
                String normalized = normalize(item.toString());
                if (!normalized.isBlank()) {
                    result.add(normalized);
                }
            }
            return result;
        }
        String normalized = normalize(value.toString());
        return normalized.isBlank() ? List.of() : List.of(normalized);
    }
}

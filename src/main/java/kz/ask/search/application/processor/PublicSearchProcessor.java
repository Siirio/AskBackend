package kz.ask.search.application.processor;

import com.fasterxml.jackson.databind.JsonNode;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import kz.ask.business.domain.BrandDropService;
import kz.ask.business.domain.BrandProfileService;
import kz.ask.business.domain.dto.BrandDropDto;
import kz.ask.business.domain.dto.BrandProfileDto;
import kz.ask.search.api.dto.SearchIntentStructureRequest;
import kz.ask.search.api.dto.SearchResultCardResponse;
import kz.ask.search.domain.SearchTermEnricher;
import kz.ask.search.domain.entity.SearchDocument;
import kz.ask.search.domain.enums.SearchDocumentType;
import kz.ask.search.infrastructure.repository.SearchDocumentRepository;
import kz.ask.search.infrastructure.repository.SearchQueryAliasRepository;
import kz.ask.shared.domain.enums.RecordStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class PublicSearchProcessor {

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
    private static final Integer MAX_VISIBLE_REASONS = 4;
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

    private final SearchDocumentRepository searchDocumentRepository;
    private final SearchQueryAliasRepository searchQueryAliasRepository;
    private final IntentCategoryMapper intentCategoryMapper;
    private final SearchTermEnricher searchTermEnricher;
    private final BrandProfileService brandProfileService;
    private final BrandDropService brandDropService;

    @Transactional(readOnly = true)
    public List<SearchResultCardResponse> search(String query, String scope, String category, Integer page, Integer size) {
        Integer safeSize = Math.min(Math.max(size == null ? DEFAULT_PAGE_SIZE : size, 1), MAX_PAGE_SIZE);
        Integer safePage = Math.max(page == null ? 0 : page, 0);
        String normalizedQuery = normalize(query);
        return search(resolveDocumentTypes(scope), resolveQueryTerms(normalizedQuery),
                normalize(category), safePage, safeSize).stream().map(this::toCard).toList();
    }

    @Transactional(readOnly = true)
    public List<SearchResultCardResponse> searchStructured(SearchPlan plan, Integer page, Integer size) {
        Integer safeSize = Math.min(Math.max(size == null ? DEFAULT_PAGE_SIZE : size, 1), MAX_PAGE_SIZE);
        Integer safePage = Math.max(page == null ? 0 : page, 0);
        return rank(searchDocumentRepository.findActiveCandidates(resolvePlanDocumentTypes(plan)), plan).stream()
                .skip((long) safePage * safeSize)
                .limit(safeSize)
                .map(this::toCard)
                .toList();
    }

    public SearchPlan buildSearchPlan(JsonNode intentStructure, SearchIntentStructureRequest request) {
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
                .build();
    }

    public String resolveStructuredScope(JsonNode intentStructure) {
        SearchDocumentType itemType = resolveStructuredItemType(intentStructure);
        if (itemType == SearchDocumentType.PRODUCT) {
            return "product";
        }
        if (itemType == SearchDocumentType.SERVICE) {
            return "service";
        }
        return "all";
    }

    public List<String> resolveStructuredQueryTerms(SearchPlan plan) {
        Set<String> terms = new LinkedHashSet<>();
        addTerms(terms, plan.getHardMatchTerms());
        addTerms(terms, plan.getQualifierTerms());
        addTerms(terms, plan.getExactTerms());
        addTerms(terms, plan.getCategoryAliases());
        addTerms(terms, plan.getSemanticTerms());
        addTerms(terms, plan.getSynonyms());
        addTerms(terms, plan.getRelatedTerms());
        addTerms(terms, plan.getMustHave());
        return terms.stream().toList();
    }

    private List<SearchDocument> search(List<SearchDocumentType> documentTypes, List<String> queryTerms,
                                        String normalizedCategory, Integer safePage, Integer safeSize) {
        List<SearchDocument> documents = new ArrayList<>();
        Set<UUID> documentIds = new LinkedHashSet<>();
        for (String queryTerm : queryTerms) {
            collectDocuments(documents, documentIds, documentTypes, queryTerm, normalizedCategory, safePage, safeSize);
            if (documents.size() >= safeSize) {
                return documents;
            }
        }
        return documents;
    }

    private void collectDocuments(List<SearchDocument> documents, Set<UUID> documentIds,
                                  List<SearchDocumentType> documentTypes, String queryTerm,
                                  String normalizedCategory, Integer safePage, Integer safeSize) {
        List<SearchDocument> found = searchDocumentRepository.search(
                documentTypes, queryTerm, normalizedCategory, PageRequest.of(safePage, safeSize)).getContent();
        for (SearchDocument document : found) {
            if (documentIds.add(document.getId())) {
                documents.add(document);
            }
            if (documents.size() >= safeSize) {
                return;
            }
        }
    }

    private List<ScoredSearchDocument> rank(List<SearchDocument> candidates, SearchPlan plan) {
        return candidates.stream()
                .map(document -> score(document, plan))
                .filter(scored -> scored.getScore() >= MINIMUM_RESULT_SCORE)
                .sorted(Comparator.comparing(this::sectionPriority)
                        .thenComparing(ScoredSearchDocument::getScore, Comparator.reverseOrder()))
                .toList();
    }

    private ScoredSearchDocument score(SearchDocument document, SearchPlan plan) {
        if (!passesHardSemanticGate(document, plan)) {
            return rejected(document);
        }
        List<String> matchReasons = new ArrayList<>();
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
        score = score - pricePenalty(document, plan, warnings) - cityPenalty(document, plan, warnings);
        addMatchReasons(matchReasons, document, plan);
        return ScoredSearchDocument.builder()
                .document(document)
                .score(score)
                .sectionType(resolveSectionType(warnings, score))
                .confidenceCode(resolveConfidenceCode(score, warnings))
                .matchReasons(matchReasons)
                .warnings(warnings)
                .build();
    }

    private ScoredSearchDocument rejected(SearchDocument document) {
        return ScoredSearchDocument.builder()
                .document(document)
                .score(0)
                .sectionType(SECTION_SIMILAR)
                .confidenceCode(CONFIDENCE_LOW)
                .matchReasons(List.of())
                .warnings(List.of())
                .build();
    }

    private Boolean passesHardSemanticGate(SearchDocument document, SearchPlan plan) {
        return !matchesNotWanted(document, plan)
                && matchesHardTerms(document, plan)
                && matchesPackageConstraint(document, plan)
                && matchesQualifierTerms(document, plan)
                && matchesRequiredFeatureTerms(document, plan);
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

    private void addMatchReasons(List<String> matchReasons, SearchDocument document, SearchPlan plan) {
        String text = documentText(document);
        plan.getHardMatchTerms().stream()
                .filter(term -> contains(text, normalize(term)))
                .forEach(term -> addTerm(matchReasons, term));
        plan.getQualifierTerms().stream()
                .filter(term -> contains(text, normalize(term)))
                .forEach(term -> addTerm(matchReasons, term));
        plan.getMustHave().stream()
                .filter(term -> contains(text, normalize(term)))
                .forEach(term -> addTerm(matchReasons, term));
        if (matchesPackageConstraint(document, plan)) {
            addTerm(matchReasons, packageMatchReason(plan));
        }
        addDefaultMatchReasons(matchReasons, document, plan);
        if (matchReasons.size() > MAX_VISIBLE_REASONS) {
            List<String> visibleReasons = new ArrayList<>(matchReasons.subList(0, MAX_VISIBLE_REASONS));
            matchReasons.clear();
            matchReasons.addAll(visibleReasons);
        }
    }

    private void addDefaultMatchReasons(List<String> matchReasons, SearchDocument document, SearchPlan plan) {
        if (document.getCategoryLabel() != null && !document.getCategoryLabel().isBlank()) {
            addTerm(matchReasons, "category: " + document.getCategoryLabel());
        }
        if (document.getBranch() != null && document.getBranch().getAddress() != null && !document.getBranch().getAddress().isBlank()) {
            addTerm(matchReasons, "pickup available");
        }
        if (plan.getMaxPrice() != null && document.getPrice() != null && document.getPrice().compareTo(plan.getMaxPrice()) <= 0) {
            addTerm(matchReasons, "within budget");
        }
        if (document.getBusiness() != null) {
            addTerm(matchReasons, "brand matches this intent");
        }
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

    private List<String> resolveQueryTerms(String normalizedQuery) {
        Set<String> queryTerms = new LinkedHashSet<>();
        addTerm(queryTerms, normalizedQuery);
        if (!normalizedQuery.isBlank()) {
            addTerms(queryTerms, resolveAliasTargets(List.of(normalizedQuery)));
        }
        return queryTerms.stream().toList();
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

    private String packageMatchReason(SearchPlan plan) {
        if (plan.getMinPackageGrams() != null) {
            return "вес от " + plan.getMinPackageGrams().stripTrailingZeros().toPlainString() + " г";
        }
        if (plan.getMaxPackageGrams() != null) {
            return "вес до " + plan.getMaxPackageGrams().stripTrailingZeros().toPlainString() + " г";
        }
        return "";
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

    private List<SearchDocumentType> resolveDocumentTypes(String scope) {
        if ("product".equalsIgnoreCase(scope)) {
            return List.of(SearchDocumentType.PRODUCT);
        }
        if ("service".equalsIgnoreCase(scope)) {
            return List.of(SearchDocumentType.SERVICE);
        }
        return List.of(SearchDocumentType.PRODUCT, SearchDocumentType.SERVICE);
    }

    private SearchResultCardResponse toCard(SearchDocument document) {
        BrandProfileDto brandProfile = resolveBrandProfile(document);
        List<BrandDropDto> drops = resolveDrops(document);
        return SearchResultCardResponse.builder()
                .id(document.getId())
                .type(document.getDocumentType().name())
                .name(document.getTitle())
                .supplierName(document.getBusiness() != null ? document.getBusiness().getName() : null)
                .brandId(document.getBusiness() != null ? document.getBusiness().getId() : null)
                .businessName(document.getBusiness() != null ? document.getBusiness().getName() : null)
                .brandColor(resolveBrandColor(brandProfile))
                .brandLogoUrl(brandProfile != null ? brandProfile.getLogoUrl() : null)
                .brandCoverUrl(brandProfile != null ? brandProfile.getCoverUrl() : null)
                .brandDescriptor(resolveBrandDescriptor(brandProfile, document))
                .branchAddress(document.getBranch() != null ? document.getBranch().getAddress() : null)
                .branchContext(resolveBranchContext(document))
                .categoryName(document.getCategoryLabel())
                .availabilityStatus("NEEDS_CONFIRMATION")
                .confirmationStatus("NOT_CONFIRMED")
                .pickupOptions(resolvePickupOptions(document))
                .distanceText(null)
                .priceText(document.getPrice() != null ? "от " + document.getPrice().toBigInteger() + " ₸" : null)
                .source(document.getSource() != null ? document.getSource() : "CATALOG")
                .sourceType(document.getSource() != null ? document.getSource() : "CATALOG")
                .publicNote(document.getPublicNote() != null ? document.getPublicNote() : "")
                .confidenceCode(CONFIDENCE_MEDIUM)
                .sectionType(SECTION_EXACT)
                .score(null)
                .matchReasons(defaultCardReasons(document))
                .badges(resolveBadges(brandProfile, document, drops))
                .warnings(List.of())
                .requiresSupplierCheck(true)
                .contactActions(List.of("CHAT", "MAP", "REQUEST"))
                .availableActions(List.of("CHAT", "MAP", "REQUEST"))
                .build();
    }

    private SearchResultCardResponse toCard(ScoredSearchDocument scored) {
        SearchDocument document = scored.getDocument();
        BrandProfileDto brandProfile = resolveBrandProfile(document);
        List<BrandDropDto> drops = resolveDrops(document);
        return SearchResultCardResponse.builder()
                .id(document.getId())
                .type(document.getDocumentType().name())
                .name(document.getTitle())
                .supplierName(document.getBusiness() != null ? document.getBusiness().getName() : null)
                .brandId(document.getBusiness() != null ? document.getBusiness().getId() : null)
                .businessName(document.getBusiness() != null ? document.getBusiness().getName() : null)
                .brandColor(resolveBrandColor(brandProfile))
                .brandLogoUrl(brandProfile != null ? brandProfile.getLogoUrl() : null)
                .brandCoverUrl(brandProfile != null ? brandProfile.getCoverUrl() : null)
                .brandDescriptor(resolveBrandDescriptor(brandProfile, document))
                .branchAddress(document.getBranch() != null ? document.getBranch().getAddress() : null)
                .branchContext(resolveBranchContext(document))
                .categoryName(document.getCategoryLabel())
                .availabilityStatus("NEEDS_CONFIRMATION")
                .confirmationStatus("NOT_CONFIRMED")
                .pickupOptions(resolvePickupOptions(document))
                .distanceText(null)
                .priceText(document.getPrice() != null ? "от " + document.getPrice().toBigInteger() + " ₸" : null)
                .source(document.getSource() != null ? document.getSource() : "CATALOG")
                .sourceType(document.getSource() != null ? document.getSource() : "CATALOG")
                .publicNote(buildPublicNote(document, scored))
                .confidenceCode(scored.getConfidenceCode())
                .sectionType(scored.getSectionType())
                .score(scored.getScore())
                .matchReasons(scored.getMatchReasons())
                .badges(resolveBadges(brandProfile, document, drops))
                .warnings(scored.getWarnings())
                .requiresSupplierCheck(true)
                .contactActions(List.of("CHAT", "MAP", "REQUEST"))
                .availableActions(List.of("CHAT", "MAP", "REQUEST"))
                .build();
    }

    private String buildPublicNote(SearchDocument document, ScoredSearchDocument scored) {
        Set<String> notes = new LinkedHashSet<>();
        addTerm(notes, document.getPublicNote());
        addTerms(notes, scored.getMatchReasons());
        addTerms(notes, scored.getWarnings());
        return String.join("; ", notes);
    }

    private BrandProfileDto resolveBrandProfile(SearchDocument document) {
        if (document.getBusiness() == null) {
            return null;
        }
        return brandProfileService.findByBusinessId(document.getBusiness().getId());
    }

    private List<BrandDropDto> resolveDrops(SearchDocument document) {
        if (document.getBusiness() == null) {
            return List.of();
        }
        return brandDropService.listPublic(document.getBusiness().getId());
    }

    private String resolveBrandColor(BrandProfileDto profile) {
        if (profile == null || profile.getBrandColor() == null || profile.getBrandColor().isBlank()) {
            return DEFAULT_BRAND_COLOR;
        }
        return profile.getBrandColor();
    }

    private String resolveBrandDescriptor(BrandProfileDto profile, SearchDocument document) {
        if (profile != null && profile.getDescription() != null && !profile.getDescription().isBlank()) {
            return profile.getDescription();
        }
        return document.getBusiness() != null ? document.getBusiness().getName() : "";
    }

    private String resolveBranchContext(SearchDocument document) {
        if (document.getBranch() == null) {
            return "";
        }
        List<String> parts = new ArrayList<>();
        addTerm(parts, document.getBranch().getName());
        addTerm(parts, document.getBranch().getAddress());
        if (document.getBranch().getCity() != null) {
            addTerm(parts, document.getBranch().getCity().getName());
        }
        return String.join(", ", parts);
    }

    private List<String> resolvePickupOptions(SearchDocument document) {
        if (document.getBranch() == null) {
            return List.of();
        }
        if (Boolean.TRUE.equals(document.getBranch().getOnlineOnly())) {
            return List.of("ONLINE");
        }
        return List.of("PICKUP");
    }

    private List<String> defaultCardReasons(SearchDocument document) {
        List<String> reasons = new ArrayList<>();
        addTerm(reasons, "matches by title");
        if (document.getCategoryLabel() != null && !document.getCategoryLabel().isBlank()) {
            addTerm(reasons, "category: " + document.getCategoryLabel());
        }
        if (document.getBranch() != null && document.getBranch().getAddress() != null && !document.getBranch().getAddress().isBlank()) {
            addTerm(reasons, "pickup available");
        }
        return reasons;
    }

    private List<String> resolveBadges(BrandProfileDto profile, SearchDocument document, List<BrandDropDto> drops) {
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
        if (!drops.isEmpty()) {
            badges.add("active drop");
        }
        return badges;
    }

    private Boolean hasOfficialLink(BrandProfileDto profile) {
        return (profile.getWebsiteUrl() != null && !profile.getWebsiteUrl().isBlank())
                || (profile.getTelegramUrl() != null && !profile.getTelegramUrl().isBlank())
                || (profile.getInstagramUrl() != null && !profile.getInstagramUrl().isBlank());
    }
}

package kz.ask.search.application.processor;

import com.fasterxml.jackson.databind.JsonNode;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import kz.ask.business.domain.BrandProfileService;
import kz.ask.business.domain.dto.BrandProfileDto;
import kz.ask.contact.api.dto.ContactActionSummaryResponse;
import kz.ask.contact.domain.ContactActionService;
import kz.ask.search.api.dto.SearchIntentStructureRequest;
import kz.ask.search.api.dto.SearchConstraintResponse;
import kz.ask.search.api.dto.SearchDiagnosticsResponse;
import kz.ask.search.api.dto.SearchLocationRequest;

import kz.ask.search.api.dto.SearchV2CardResponse;
import kz.ask.search.api.dto.SearchV2Request;
import kz.ask.search.api.dto.SearchV2Response;
import kz.ask.search.api.dto.SearchV2SectionResponse;
import kz.ask.search.domain.AttributeKeys;
import kz.ask.search.domain.MeilisearchService;
import kz.ask.search.domain.SearchIntentStructurer;
import kz.ask.search.domain.SearchTermEnricher;
import kz.ask.search.domain.entity.SearchDocument;
import kz.ask.search.domain.enums.SearchAvailabilityStatus;
import kz.ask.search.domain.enums.SearchDocumentType;
import kz.ask.search.infrastructure.repository.SearchDocumentRepository;
import kz.ask.search.infrastructure.repository.SearchQueryAliasRepository;
import kz.ask.shared.domain.enums.RecordStatus;
import kz.ask.shared.util.DistanceCalculator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class StructuredSearchProcessor {

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
    private static final Integer MAX_CANDIDATES = 200;
    private static final Pattern MAX_PRICE_PATTERN = Pattern.compile(
            "(?:до|не\\s+дороже|максимум|under|below|up\\s+to|max(?:imum)?|no\\s+more\\s+than)\\s*(\\d+[\\d\\s]*)(к|k|тыс|тысяч|тг)?");
    private static final Pattern MIN_PRICE_PATTERN = Pattern.compile(
            "(?:от|минимум|over|above|from|min(?:imum)?|at\\s+least)\\s*(\\d+[\\d\\s]*)(к|k|тыс|тысяч|тг)?");
    private static final Pattern PRICE_RANGE_PATTERN = Pattern.compile(
            "(?:between\\s+)?(\\d+[\\d\\s]*)(к|k|тыс|тысяч|тг)?\\s*(?:-|to|and)\\s*(\\d+[\\d\\s]*)(к|k|тыс|тысяч|тг)?");
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
    private final ContactActionService contactActionService;
    private final MeilisearchService meilisearchService;

    @Transactional(readOnly = true)
    public SearchV2Response search(SearchV2Request request) {
        long startedAt = System.nanoTime();
        int page = request.getPage() == null ? 0 : request.getPage();
        int pageSize = request.getPageSize() == null ? DEFAULT_PAGE_SIZE : request.getPageSize();
        int requestedWindow = Math.min(MAX_CANDIDATES, (page + 1) * pageSize);
        int candidateLimit = Math.min(MAX_CANDIDATES, Math.max(requestedWindow * 3, pageSize * 3));
        SearchIntentStructureRequest aiRequest = toIntentRequest(request);
        JsonNode intentStructure = searchIntentStructurer.structure(aiRequest);
        SearchPlan searchPlan = buildSearchPlan(intentStructure, aiRequest);
        SearchLocationRequest userLocation = request.getUserLocation();

        List<ScoredSearchDocument> exactCandidates;
        boolean fallbackUsed = false;
        String fallbackReason = null;
        String engine = "MEILISEARCH";
        try {
            exactCandidates = searchViaMeilisearch(searchPlan, searchPlan, userLocation, candidateLimit);
        } catch (Exception e) {
            fallbackReason = rootCauseMessage(e);
            log.warn("""

                --------------------------------------------------------------------

                Meilisearch is not available {}, falling back to PostgreSql

                ----------------------------------------------------------------------
                """, fallbackReason);
            fallbackUsed = true;
            engine = "POSTGRESQL";
            exactCandidates = searchViaPostgres(searchPlan, searchPlan, userLocation, candidateLimit);
        }

        List<ScoredSearchDocument> exact = exactCandidates.stream()
                .filter(scored -> scored.getWarnings().isEmpty())
                .toList();
        List<ScoredSearchDocument> alternatives = List.of();
        if (exact.size() < requestedWindow && hasRelaxableConstraints(searchPlan)) {
            SearchPlan relaxedPlan = searchPlan.toBuilder()
                    .city("")
                    .minPrice(null)
                    .maxPrice(null)
                    .build();
            List<ScoredSearchDocument> relaxed = fallbackUsed
                    ? searchViaPostgres(relaxedPlan, searchPlan, userLocation, candidateLimit)
                    : searchViaMeilisearch(relaxedPlan, searchPlan, userLocation, candidateLimit);
            Set<UUID> exactIds = exact.stream().map(scored -> scored.getDocument().getId()).collect(Collectors.toSet());
            alternatives = relaxed.stream()
                    .filter(scored -> !scored.getWarnings().isEmpty())
                    .filter(scored -> !exactIds.contains(scored.getDocument().getId()))
                    .toList();
        }

        List<ScoredSearchDocument> ordered = new ArrayList<>(exact);
        ordered.addAll(alternatives);
        ordered = sort(ordered, searchPlan.getSort());
        int fromIndex = Math.min(page * pageSize, ordered.size());
        int toIndex = Math.min(fromIndex + pageSize, ordered.size());
        List<ScoredSearchDocument> pageResults = ordered.subList(fromIndex, toIndex);
        boolean hasNext = ordered.size() > toIndex || ordered.size() >= candidateLimit;
        Set<UUID> businessIds = pageResults.stream()
                .map(scored -> scored.getDocument().getBusiness())
                .filter(Objects::nonNull)
                .map(business -> business.getId())
                .collect(Collectors.toSet());
        Map<UUID, BrandProfileDto> brandProfiles = brandProfileService.findByBusinessIds(businessIds);
        Map<UUID, List<ContactActionSummaryResponse>> contactActions =
                contactActionService.summarize(businessIds);

        return SearchV2Response.builder()
                .rawQuery(request.getRawQuery())
                .scope(resolveScope(searchPlan))
                .understoodQuery(request.getRawQuery())
                .interpretedConstraints(toConstraints(searchPlan))
                .sections(toSections(pageResults, brandProfiles, contactActions, request.getLanguage()))
                .page(page)
                .pageSize(pageSize)
                .total(ordered.size())
                .hasNext(hasNext)
                .diagnostics(SearchDiagnosticsResponse.builder()
                        .engine(engine)
                        .fallbackUsed(fallbackUsed)
                        .fallbackReason(fallbackReason)
                        .candidateCount(exactCandidates.size())
                        .latencyMs((System.nanoTime() - startedAt) / 1_000_000L)
                        .build())
                .build();
    }

    private String rootCauseMessage(Throwable failure) {
        Throwable rootCause = failure;
        while (rootCause.getCause() != null) {
            rootCause = rootCause.getCause();
        }
        String message = rootCause.getMessage();
        return message == null || message.isBlank()
                ? rootCause.getClass().getSimpleName()
                : message;
    }

    private List<ScoredSearchDocument> searchViaMeilisearch(SearchPlan retrievalPlan, SearchPlan scoringPlan,
                                                             SearchLocationRequest userLocation, Integer candidateLimit) {
        List<UUID> rankedIds = meilisearchService.search(retrievalPlan, candidateLimit);
        if (rankedIds.isEmpty()) {
            return List.of();
        }

        Map<UUID, SearchDocument> docsById = searchDocumentRepository.findAllByIdIn(rankedIds).stream()
                .collect(LinkedHashMap::new, (m, d) -> m.put(d.getId(), d), LinkedHashMap::putAll);

        List<ScoredSearchDocument> scored = new ArrayList<>();
        for (int i = 0; i < rankedIds.size(); i++) {
            SearchDocument doc = docsById.get(rankedIds.get(i));
            if (doc == null) {
                continue;
            }
            if (!passesHardSemanticGate(doc, scoringPlan)) {
                continue;
            }
            int baseScore = 80 - (i * 2);
            List<String> warnings = new ArrayList<>();
            int adjustedScore = baseScore
                    - pricePenalty(doc, scoringPlan, warnings)
                    - cityPenalty(doc, scoringPlan, warnings);
            Integer distanceMeters = null;
            String distanceText = null;
            if (userLocation != null && doc.getBranch() != null
                    && doc.getBranch().getLatitude() != null && doc.getBranch().getLongitude() != null) {
                double km = DistanceCalculator.km(
                        userLocation.getLat(), userLocation.getLng(),
                        doc.getBranch().getLatitude().doubleValue(),
                        doc.getBranch().getLongitude().doubleValue());
                distanceMeters = DistanceCalculator.meters(
                        userLocation.getLat(), userLocation.getLng(),
                        doc.getBranch().getLatitude().doubleValue(),
                        doc.getBranch().getLongitude().doubleValue());
                distanceText = formatDistance(distanceMeters);
                adjustedScore = (int) (adjustedScore * Math.max(0.3, 1.0 / (1.0 + km * 0.5)));
            }
            scored.add(ScoredSearchDocument.builder()
                    .document(doc)
                    .score(Math.max(adjustedScore, 0))
                    .sectionType(resolveSectionType(warnings, adjustedScore))
                    .confidenceCode(resolveConfidenceCode(adjustedScore, warnings))
                    .warnings(warnings)
                    .distanceMeters(distanceMeters)
                    .distanceText(distanceText)
                    .build());
        }

        return scored.stream()
                .sorted(Comparator.comparing(this::sectionPriority)
                        .thenComparing(ScoredSearchDocument::getScore, Comparator.reverseOrder()))
                .toList();
    }

    private List<ScoredSearchDocument> searchViaPostgres(SearchPlan retrievalPlan, SearchPlan scoringPlan,
                                                          SearchLocationRequest userLocation, Integer candidateLimit) {
        List<String> documentTypes = resolvePlanDocumentTypes(retrievalPlan).stream().map(Enum::name).toList();
        List<UUID> candidateIds = searchDocumentRepository.findPostgresCandidateIds(
                documentTypes,
                postgresQuery(retrievalPlan),
                normalize(retrievalPlan.getUserSelectedCategory()),
                retrievalPlan.getMinPrice(),
                retrievalPlan.getMaxPrice(),
                normalize(retrievalPlan.getCity()),
                candidateLimit);
        Map<UUID, SearchDocument> documents = searchDocumentRepository.findAllByIdIn(candidateIds).stream()
                .collect(Collectors.toMap(SearchDocument::getId, document -> document));
        List<SearchDocument> ordered = candidateIds.stream().map(documents::get).filter(Objects::nonNull).toList();
        return rank(ordered, scoringPlan, userLocation);
    }

    private String postgresQuery(SearchPlan plan) {
        return Stream.of(plan.getExactTerms(), plan.getSemanticTerms(), plan.getHardMatchTerms())
                .flatMap(List::stream)
                .map(this::normalize)
                .filter(term -> !term.isBlank())
                .findFirst()
                .orElse("");
    }

    private boolean hasRelaxableConstraints(SearchPlan plan) {
        return !plan.getCity().isBlank() || plan.getMinPrice() != null || plan.getMaxPrice() != null;
    }

    private List<ScoredSearchDocument> sort(List<ScoredSearchDocument> documents, String sort) {
        Comparator<ScoredSearchDocument> withinSection = switch (normalizeSort(sort)) {
            case "distance" -> Comparator.comparing(
                    ScoredSearchDocument::getDistanceMeters,
                    Comparator.nullsLast(Comparator.naturalOrder()));
            case "price_asc" -> Comparator.comparing(
                    scored -> scored.getDocument().getPrice(),
                    Comparator.nullsLast(Comparator.naturalOrder()));
            default -> Comparator.comparing(ScoredSearchDocument::getScore).reversed();
        };
        return documents.stream()
                .sorted(Comparator.comparing(this::sectionPriority).thenComparing(withinSection))
                .toList();
    }

    private List<SearchConstraintResponse> toConstraints(SearchPlan plan) {
        List<SearchConstraintResponse> constraints = new ArrayList<>();
        addConstraint(constraints, "scope", resolveScope(plan), "INTERPRETED");
        addConstraint(constraints, "category", plan.getUserSelectedCategory(), "EXPLICIT");
        addConstraint(constraints, "city", plan.getCity(), "EXPLICIT");
        addConstraint(constraints, "min_price", plan.getMinPrice(), "EXPLICIT_OR_QUERY");
        addConstraint(constraints, "max_price", plan.getMaxPrice(), "EXPLICIT_OR_QUERY");
        addConstraint(constraints, "min_package_grams", plan.getMinPackageGrams(), "QUERY");
        addConstraint(constraints, "max_package_grams", plan.getMaxPackageGrams(), "QUERY");
        return constraints;
    }

    private void addConstraint(List<SearchConstraintResponse> constraints, String key, Object value, String source) {
        if (value == null || value.toString().isBlank() || "ALL".equals(value)) {
            return;
        }
        constraints.add(SearchConstraintResponse.builder()
                .key(key)
                .value(value.toString())
                .source(source)
                .build());
    }

    private List<SearchV2SectionResponse> toSections(
            List<ScoredSearchDocument> results,
            Map<UUID, BrandProfileDto> brandProfiles,
            Map<UUID, List<ContactActionSummaryResponse>> contactActions,
            String language) {
        List<ScoredSearchDocument> exact = results.stream()
                .filter(scored -> scored.getWarnings().isEmpty())
                .toList();
        List<ScoredSearchDocument> alternatives = results.stream()
                .filter(scored -> !scored.getWarnings().isEmpty())
                .toList();
        List<SearchV2SectionResponse> sections = new ArrayList<>();
        if (!exact.isEmpty()) {
            sections.add(SearchV2SectionResponse.builder()
                    .type("exact")
                    .kind("EXACT")
                    .title(localized(language, "Совпадения", "Сәйкестіктер", "Matches"))
                    .relaxedConstraints(List.of())
                    .cards(exact.stream().map(scored -> toV2Card(scored, brandProfiles, contactActions, language)).toList())
                    .build());
        }
        if (!alternatives.isEmpty()) {
            List<String> relaxed = alternatives.stream()
                    .flatMap(scored -> scored.getWarnings().stream())
                    .map(this::constraintForWarning)
                    .distinct()
                    .toList();
            sections.add(SearchV2SectionResponse.builder()
                    .type("alternatives")
                    .kind("ALTERNATIVE")
                    .title(localized(language, "Альтернативы", "Балама нұсқалар", "Alternatives"))
                    .relaxedConstraints(relaxed)
                    .reason(alternativeReason(relaxed, language))
                    .cards(alternatives.stream().map(scored -> toV2Card(scored, brandProfiles, contactActions, language)).toList())
                    .build());
        }
        return sections;
    }

    private String constraintForWarning(String warning) {
        return switch (warning) {
            case "OVER_BUDGET" -> "max_price";
            case "UNDER_BUDGET" -> "min_price";
            case "PRICE_UNKNOWN" -> "price";
            case "WRONG_CITY", "CITY_UNKNOWN" -> "city";
            default -> warning.toLowerCase(Locale.ROOT);
        };
    }

    private String alternativeReason(List<String> relaxed, String language) {
        return relaxed.isEmpty()
                ? localized(language, "Похожие результаты", "Ұқсас нәтижелер", "Related results")
                : localized(language,
                        "Дополнительных точных совпадений нет. Ослабленные условия: ",
                        "Қосымша дәл сәйкестік табылмады. Жеңілдетілген шарттар: ",
                        "No additional exact matches were found; relaxed constraints: ")
                        + relaxed.stream().map(constraint -> localizedConstraint(constraint, language)).collect(Collectors.joining(", "));
    }

    private SearchIntentStructureRequest toIntentRequest(SearchV2Request request) {
        SearchIntentStructureRequest aiRequest = new SearchIntentStructureRequest();
        aiRequest.setRawQuery(request.getRawQuery());
        aiRequest.setSelectedMode(resolveMode(effectiveScope(request)));
        aiRequest.setSelectedCategory(effectiveCategory(request));
        aiRequest.setCity(effectiveCity(request));
        aiRequest.setSort(normalizeSort(request.getSort()));
        aiRequest.setUserLocation(request.getUserLocation());
        aiRequest.setLanguage(request.getLanguage());
        aiRequest.setExplicitMinPrice(effectiveMinPrice(request));
        aiRequest.setExplicitMaxPrice(effectiveMaxPrice(request));
        return aiRequest;
    }

    private String effectiveScope(SearchV2Request request) {
        if (request.getOverrides() != null && request.getOverrides().getScope() != null) {
            return request.getOverrides().getScope();
        }
        if (request.getFilters() != null && request.getFilters().getScope() != null) {
            return request.getFilters().getScope();
        }
        return request.getScope();
    }

    private String effectiveCategory(SearchV2Request request) {
        if (request.getOverrides() != null && request.getOverrides().getCategory() != null) {
            return request.getOverrides().getCategory();
        }
        if (request.getFilters() != null && request.getFilters().getCategory() != null) {
            return request.getFilters().getCategory();
        }
        return request.getSelectedCategory();
    }

    private String effectiveCity(SearchV2Request request) {
        if (request.getOverrides() != null && request.getOverrides().getCity() != null) {
            return request.getOverrides().getCity();
        }
        if (request.getFilters() != null && request.getFilters().getCity() != null) {
            return request.getFilters().getCity();
        }
        return request.getCity();
    }

    private BigDecimal effectiveMinPrice(SearchV2Request request) {
        if (request.getOverrides() != null && request.getOverrides().getMinPrice() != null) {
            return request.getOverrides().getMinPrice();
        }
        return request.getFilters() == null ? null : request.getFilters().getMinPrice();
    }

    private BigDecimal effectiveMaxPrice(SearchV2Request request) {
        if (request.getOverrides() != null && request.getOverrides().getMaxPrice() != null) {
            return request.getOverrides().getMaxPrice();
        }
        return request.getFilters() == null ? null : request.getFilters().getMaxPrice();
    }

    private String normalizeSort(String sort) {
        if ("distance".equalsIgnoreCase(sort)) {
            return "distance";
        }
        if ("price_asc".equalsIgnoreCase(sort) || "lowest_price".equalsIgnoreCase(sort)) {
            return "price_asc";
        }
        return "relevance";
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
                .sort(normalizeSort(request.getSort()))
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
        if (document.getPrice() == null && (plan.getMinPrice() != null || plan.getMaxPrice() != null)) {
            warnings.add("PRICE_UNKNOWN");
            return OVER_BUDGET_PENALTY;
        }
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
        if (plan.getCity().isBlank()) {
            return 0;
        }
        if (document.getBranch() == null || document.getBranch().getCity() == null) {
            warnings.add("CITY_UNKNOWN");
            return WRONG_CITY_PENALTY;
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
        String sourceText = normalize(request.getRawQuery());
        addKnownIntentTerms(terms, sourceText);
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
        addTerm(terms, removePriceWording(request.getRawQuery()));
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
        if (request.getExplicitMinPrice() != null) {
            return request.getExplicitMinPrice();
        }
        return parsePrice(request.getRawQuery(), MIN_PRICE_PATTERN, true);
    }

    private BigDecimal resolveMaxPrice(JsonNode intentStructure, SearchIntentStructureRequest request) {
        if (request.getExplicitMaxPrice() != null) {
            return request.getExplicitMaxPrice();
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

    private SearchV2CardResponse toV2Card(
            ScoredSearchDocument scored,
            Map<UUID, BrandProfileDto> brandProfiles,
            Map<UUID, List<ContactActionSummaryResponse>> contactActions,
            String language) {
        SearchDocument document = scored.getDocument();
        UUID businessId = document.getBusiness() == null ? null : document.getBusiness().getId();
        BrandProfileDto brandProfile = businessId == null ? null : brandProfiles.get(businessId);
        return SearchV2CardResponse.builder()
                .component(component(document.getDocumentType().name()))
                .resultId(document.getId())
                .businessId(document.getBusiness() != null ? document.getBusiness().getId() : null)
                .businessName(document.getBusiness() != null ? document.getBusiness().getName() : null)
                .brandColor(resolveBrandColor(brandProfile))
                .brandLogoUrl(brandProfile != null ? brandProfile.getLogoUrl() : null)
                .title(document.getTitle())
                .price(document.getPrice())
                .availability(document.getAvailabilityStatus().name())
                .availabilityWarning(document.getAvailabilityStatus() == SearchAvailabilityStatus.UNKNOWN
                        ? localized(language,
                                "Наличие не подтверждено компанией",
                                "Қолжетімділікті компания растаған жоқ",
                                "Availability has not been confirmed by the business")
                        : null)
                .matchReasons(matchReasons(scored, language))
                .badges(resolveBadges(brandProfile, document))
                .distanceMeters(scored.getDistanceMeters())
                .branchName(document.getBranch() != null ? document.getBranch().getName() : null)
                .contactActions(businessId == null ? List.of() : contactActions.getOrDefault(businessId, List.of()))
                .build();
    }

    private List<String> matchReasons(ScoredSearchDocument scored, String language) {
        List<String> reasons = new ArrayList<>();
        SearchDocument document = scored.getDocument();
        if (scored.getScore() >= MINIMUM_RESULT_SCORE) {
            reasons.add(localized(language,
                    "Соответствует запросу",
                    "Сұрауға сәйкес келеді",
                    "Matches the requested product or service"));
        }
        if (document.getCategoryLabel() != null && !document.getCategoryLabel().isBlank()) {
            reasons.add(localized(language, "Категория: ", "Санат: ", "Category: ") + document.getCategoryLabel());
        }
        if (scored.getWarnings().isEmpty() && document.getPrice() != null) {
            reasons.add(localized(language,
                    "Соответствует выбранным ценовым условиям",
                    "Таңдалған баға шарттарына сәйкес келеді",
                    "Matches the selected price constraints"));
        }
        return reasons.stream().limit(3).toList();
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
            case "price" -> localized(language, "цена", "баға", "price");
            default -> constraint;
        };
    }

    private String resolveBrandColor(BrandProfileDto profile) {
        if (profile == null || profile.getBrandColor() == null || profile.getBrandColor().isBlank()) {
            return DEFAULT_BRAND_COLOR;
        }
        return profile.getBrandColor();
    }

    private List<String> resolveBadges(BrandProfileDto profile, SearchDocument document) {
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
        Map<String, Object> docAttrs = combinedAttributes(document);
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
        Map<String, Object> docAttrs = combinedAttributes(document);
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

    private Map<String, Object> combinedAttributes(SearchDocument document) {
        Map<String, Object> attributes = new HashMap<>();
        if (document.getAiAttributes() != null) {
            attributes.putAll(document.getAiAttributes());
        }
        if (document.getVerifiedAttributes() != null) {
            attributes.putAll(document.getVerifiedAttributes());
        }
        return attributes;
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

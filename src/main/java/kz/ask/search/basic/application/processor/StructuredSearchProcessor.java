package kz.ask.search.basic.application.processor;

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
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import kz.ask.business.profile.domain.BusinessProfileService;
import kz.ask.business.profile.domain.dto.BusinessProfileDto;
import kz.ask.search.search_query_enrichment.api.dto.SearchIntentStructureRequest;
import kz.ask.search.search_query_enrichment.domain.SearchIntentStructurer;
import kz.ask.search.search_query_enrichment.domain.SearchTermEnricher;
import kz.ask.search.basic.api.dto.SearchConstraintResponse;
import kz.ask.search.basic.api.dto.SearchDiagnosticsResponse;
import kz.ask.search.basic.api.dto.SearchLocationRequest;

import kz.ask.search.basic.api.dto.SearchCardResponse;
import kz.ask.search.basic.api.dto.SearchBusinessProfileResponse;
import kz.ask.search.basic.api.dto.SearchRequest;
import kz.ask.search.basic.api.dto.SearchResponse;
import kz.ask.search.basic.api.dto.SearchSectionResponse;
import kz.ask.search.basic.domain.AttributeKeys;
import kz.ask.search.basic.domain.MeilisearchService;
import kz.ask.search.basic.domain.entity.SearchDocument;
import kz.ask.search.basic.domain.enums.SearchAvailabilityStatus;
import kz.ask.search.basic.domain.enums.SearchDocumentType;
import kz.ask.search.basic.infrastructure.repository.SearchDocumentRepository;
import kz.ask.search.basic.infrastructure.repository.SearchQueryAliasRepository;

import kz.ask.shared.util.DistanceCalculator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

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
    private static final Pattern MIN_PACKAGE_PATTERN = Pattern.compile(
            "(?:>|от|больше|свыше|не\\s+меньше)\\s*(\\d+[\\d\\s.,]*)(?:\\s*)(кг|kg|килограмм|килограмма|килограммов|г|гр|g|gram|грамм|грамма|граммов)");
    private static final Pattern MAX_PACKAGE_PATTERN = Pattern.compile(
            "(?:<|до|меньше|не\\s+больше)\\s*(\\d+[\\d\\s.,]*)(?:\\s*)(кг|kg|килограмм|килограмма|килограммов|г|гр|g|gram|грамм|грамма|граммов)");

    private final SearchIntentStructurer searchIntentStructurer;
    private final SearchDocumentRepository searchDocumentRepository;
    private final SearchQueryAliasRepository searchQueryAliasRepository;
    private final SearchTermEnricher searchTermEnricher;
    private final BusinessProfileService businessProfileService;
    private final MeilisearchService meilisearchService;

    public SearchResponse search(SearchRequest request) {
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

        List<ScoredSearchDocument> unindexedOverlay = overlayDirtyCandidates(searchPlan, userLocation,
                exactCandidates.stream().map(scored -> scored.getDocument().getAggregateId()).collect(Collectors.toSet()));

        List<ScoredSearchDocument> exact = Stream.concat(exactCandidates.stream(), unindexedOverlay.stream())
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
        Map<UUID, BusinessProfileDto> businessProfiles = businessProfileService.findByBusinessIds(businessIds);

        return SearchResponse.builder()
                .rawQuery(request.getRawQuery())
                .mode(resolveMode(searchPlan))
                .understoodQuery(request.getRawQuery())
                .interpretedConstraints(toConstraints(searchPlan))
                .sections(toSections(pageResults, businessProfiles, request.getLocale()))
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

        List<SearchDocumentType> retrievalTypes = resolvePlanDocumentTypes(retrievalPlan);
        Map<UUID, SearchDocument> docsById = searchDocumentRepository
                .findAllByDocumentTypeAndAggregateIdIn(retrievalTypes, rankedIds).stream()
                .collect(LinkedHashMap::new, (m, d) -> m.put(d.getAggregateId(), d), LinkedHashMap::putAll);

        List<ScoredSearchDocument> scored = new ArrayList<>();
        for (int i = 0; i < rankedIds.size(); i++) {
            SearchDocument doc = docsById.get(rankedIds.get(i));
            if (doc == null) {
                continue;
            }
            int baseScore = 80 - (i * 2);
            List<String> warnings = new ArrayList<>();
            int adjustedScore = baseScore
                    - pricePenalty(doc, scoringPlan, warnings)
                    - cityPenalty(doc, scoringPlan, warnings);
            Integer distanceMeters = null;
            String distanceText = null;
            if ("distance".equals(scoringPlan.getSort()) && userLocation != null && doc.getBranch() != null
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
                .filter(candidate -> candidate.getScore() >= MINIMUM_RESULT_SCORE)
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

    private List<ScoredSearchDocument> overlayDirtyCandidates(SearchPlan plan, SearchLocationRequest userLocation,
                                                              Set<UUID> alreadyFoundAggregateIds) {
        String query = postgresQuery(plan);
        if (query.isBlank()) {
            return List.of();
        }
        List<SearchDocumentType> types = resolvePlanDocumentTypes(plan);
        List<String> documentTypes = types.stream().map(Enum::name).toList();
        List<UUID> dirtyIds = searchDocumentRepository.findDirtyCandidateIds(
                documentTypes,
                query,
                normalize(plan.getUserSelectedCategory()),
                plan.getMinPrice(),
                plan.getMaxPrice(),
                normalize(plan.getCity()),
                50);
        if (dirtyIds.isEmpty()) {
            return List.of();
        }
        Map<UUID, SearchDocument> documents = searchDocumentRepository.findAllByIdIn(dirtyIds).stream()
                .collect(Collectors.toMap(SearchDocument::getId, document -> document));
        return dirtyIds.stream()
                .map(documents::get)
                .filter(Objects::nonNull)
                .filter(doc -> !alreadyFoundAggregateIds.contains(doc.getAggregateId()))
                .map(doc -> score(doc, plan, userLocation))
                .filter(scored -> scored.getScore() >= MINIMUM_RESULT_SCORE)
                .toList();
    }

    private String postgresQuery(SearchPlan plan) {
        return Stream.concat(Stream.of(plan.getRawQuery()),
                        Stream.of(plan.getExactTerms(), plan.getExpandedTerms(), plan.getHardMatchTerms(),
                                        plan.getAiSynonyms(), plan.getRelatedTerms())
                                .flatMap(List::stream))
                .map(this::normalize)
                .filter(term -> !term.isBlank())
                .distinct()
                .limit(20)
                .collect(Collectors.joining(" OR "));
    }

    private boolean hasRelaxableConstraints(SearchPlan plan) {
        return !plan.getCity().isBlank() || plan.getMinPrice() != null || plan.getMaxPrice() != null
                || plan.getMinPackageGrams() != null || plan.getMaxPackageGrams() != null;
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
        addConstraint(constraints, "mode", resolveMode(plan), "EXPLICIT");
        addConstraint(constraints, "category", plan.getUserSelectedCategory(), "EXPLICIT");
        addConstraint(constraints, "city", plan.getCity(), "EXPLICIT");
        addConstraint(constraints, "min_price", plan.getMinPrice(), "EXPLICIT_OR_QUERY");
        addConstraint(constraints, "max_price", plan.getMaxPrice(), "EXPLICIT_OR_QUERY");
        addConstraint(constraints, "min_package_grams", plan.getMinPackageGrams(), "QUERY");
        addConstraint(constraints, "max_package_grams", plan.getMaxPackageGrams(), "QUERY");
        return constraints;
    }

    private void addConstraint(List<SearchConstraintResponse> constraints, String key, Object value, String source) {
        if (value == null || value.toString().isBlank()) {
            return;
        }
        constraints.add(SearchConstraintResponse.builder()
                .key(key)
                .value(value.toString())
                .source(source)
                .build());
    }

    private List<SearchSectionResponse> toSections(
            List<ScoredSearchDocument> results,
            Map<UUID, BusinessProfileDto> businessProfiles,
            String language) {
        List<ScoredSearchDocument> exact = results.stream()
                .filter(scored -> scored.getWarnings().isEmpty())
                .toList();
        List<ScoredSearchDocument> alternatives = results.stream()
                .filter(scored -> !scored.getWarnings().isEmpty())
                .toList();
        List<SearchSectionResponse> sections = new ArrayList<>();
        if (!exact.isEmpty()) {
            sections.add(SearchSectionResponse.builder()
                    .type("exact")
                    .kind("EXACT")
                    .title(localized(language, "Совпадения", "Сәйкестіктер", "Matches"))
                    .relaxedConstraints(List.of())
                    .cards(exact.stream().map(scored -> toV2Card(scored, businessProfiles, language)).toList())
                    .build());
        }
        if (!alternatives.isEmpty()) {
            List<String> relaxed = alternatives.stream()
                    .flatMap(scored -> scored.getWarnings().stream())
                    .map(this::constraintForWarning)
                    .distinct()
                    .toList();
            sections.add(SearchSectionResponse.builder()
                    .type("alternatives")
                    .kind("ALTERNATIVE")
                    .title(localized(language, "Альтернативы", "Балама нұсқалар", "Alternatives"))
                    .relaxedConstraints(relaxed)
                    .reason(alternativeReason(relaxed, language))
                    .cards(alternatives.stream().map(scored -> toV2Card(scored, businessProfiles, language)).toList())
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

    private SearchIntentStructureRequest toIntentRequest(SearchRequest request) {
        SearchIntentStructureRequest aiRequest = new SearchIntentStructureRequest();
        aiRequest.setRawQuery(request.getRawQuery());
        aiRequest.setSelectedMode(request.getMode().name());
        aiRequest.setSelectedCategory(request.getExplicitFilters() == null
                ? null : request.getExplicitFilters().getCategory());
        aiRequest.setCity(request.getExplicitFilters() == null
                ? null : request.getExplicitFilters().getCity());
        aiRequest.setSort(normalizeSort(request.getSort()));
        aiRequest.setUserLocation(request.getUserLocation());
        aiRequest.setLanguage(request.getLocale());
        aiRequest.setExplicitMinPrice(request.getExplicitFilters() == null
                ? null : request.getExplicitFilters().getMinPrice());
        aiRequest.setExplicitMaxPrice(request.getExplicitFilters() == null
                ? null : request.getExplicitFilters().getMaxPrice());
        return aiRequest;
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

    private String resolveMode(SearchPlan plan) {
        if (plan.getItemType() == SearchDocumentType.ITEM) {
            return "ITEM";
        }
        if (plan.getItemType() == SearchDocumentType.SERVICE) {
            return "SERVICE";
        }
        return "SERVICE";
    }

    SearchPlan buildSearchPlan(JsonNode intentStructure, SearchIntentStructureRequest request) {
        List<String> categoryAliases = collectCategoryInputs(intentStructure, request);
        List<String> exactTerms = resolveExactTerms(intentStructure, request);
        List<String> expandedTerms = resolveExpandedTerms(intentStructure);
        List<String> relatedTerms = combineTerms(resolveRelatedTerms(intentStructure), resolveAliasTargets(exactTerms),
                resolveAliasTargets(categoryAliases), resolveAliasTargets(expandedTerms));
        return SearchPlan.builder()
                .rawQuery(request.getRawQuery())
                .itemType(SearchDocumentType.valueOf(request.getSelectedMode()))
                .city(normalize(request.getCity()))
                .possibleCity(normalize(intentStructure.path("city").asText("")))
                .userSelectedCategory(normalize(request.getSelectedCategory()))
                .sort(normalizeSort(request.getSort()))
                .minPrice(request.getExplicitMinPrice())
                .maxPrice(request.getExplicitMaxPrice())
                .possibleMinPrice(parsePrice(request.getRawQuery(), MIN_PRICE_PATTERN, true))
                .possibleMaxPrice(resolvePossibleMaxPrice(request))
                .minPackageGrams(resolveMinPackageGrams(request))
                .maxPackageGrams(resolveMaxPackageGrams(request))
                .canonicalCategoryKeys(List.of())
                .categoryAliases(categoryAliases)
                .hardMatchTerms(resolveHardMatchTerms(intentStructure, request))
                .qualifierTerms(resolveQualifierTerms(intentStructure, request))
                .exactTerms(exactTerms)
                .expandedTerms(expandedTerms)
                .aiSynonyms(resolveArray(intentStructure.path("semantic").path("synonyms")))
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
        List<String> warnings = new ArrayList<>();
        Integer score = HARD_MATCH_SCORE;
        score += scoreTerms(document, plan.getHardMatchTerms(), TITLE_MATCH_SCORE, TOKEN_MATCH_SCORE, BODY_MATCH_SCORE);
        score += scoreTerms(document, plan.getQualifierTerms(), TITLE_MATCH_SCORE, TOKEN_MATCH_SCORE, BODY_MATCH_SCORE);
        score += scoreTerms(document, plan.getExactTerms(), TITLE_MATCH_SCORE, TOKEN_MATCH_SCORE, BODY_MATCH_SCORE);
        score += scoreTerms(document, plan.getMustHave(), MUST_HAVE_SCORE, MUST_HAVE_SCORE, MUST_HAVE_SCORE);
        score += scoreTerms(document, plan.getCategoryAliases(), CATEGORY_MATCH_SCORE, CATEGORY_MATCH_SCORE, CATEGORY_MATCH_SCORE);
        score += scoreTerms(document, plan.getExpandedTerms(), TITLE_MATCH_SCORE, TOKEN_MATCH_SCORE, BODY_MATCH_SCORE);
        score += scoreTerms(document, plan.getAiSynonyms(), CATEGORY_MATCH_SCORE, CATEGORY_MATCH_SCORE, NICE_TO_HAVE_SCORE);
        score += scoreTerms(document, plan.getNiceToHave(), NICE_TO_HAVE_SCORE, NICE_TO_HAVE_SCORE, NICE_TO_HAVE_SCORE);
        score += scoreStructuredAttributes(document, plan);
        score = score - pricePenalty(document, plan, warnings) - cityPenalty(document, plan, warnings);
        Integer distanceMeters = null;
        String distanceText = null;
        if ("distance".equals(plan.getSort()) && userLocation != null && document.getBranch() != null
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

    private Integer pricePenalty(SearchDocument document, SearchPlan plan, List<String> warnings) {
        boolean hasHardFilter = plan.getMinPrice() != null || plan.getMaxPrice() != null;
        boolean hasSoftSignal = plan.getPossibleMinPrice() != null || plan.getPossibleMaxPrice() != null;
        if (document.getPrice() == null && (hasHardFilter || hasSoftSignal)) {
            if (hasHardFilter) {
                warnings.add("PRICE_UNKNOWN");
                return OVER_BUDGET_PENALTY;
            }
            return 0;
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
        int softAdjustment = 0;
        if (plan.getPossibleMaxPrice() != null && document.getPrice().compareTo(plan.getPossibleMaxPrice()) > 0) {
            softAdjustment += 10;
        }
        if (plan.getPossibleMinPrice() != null && document.getPrice().compareTo(plan.getPossibleMinPrice()) < 0) {
            softAdjustment += 10;
        }
        return softAdjustment;
    }

    private Integer cityPenalty(SearchDocument document, SearchPlan plan, List<String> warnings) {
        boolean hasHardCity = !plan.getCity().isBlank();
        boolean hasSoftCity = !plan.getPossibleCity().isBlank();
        if (!hasHardCity && !hasSoftCity) {
            return 0;
        }
        if (document.getBranch() == null || document.getBranch().getCity() == null) {
            if (hasHardCity) {
                warnings.add("CITY_UNKNOWN");
                return WRONG_CITY_PENALTY;
            }
            return 0;
        }
        String docCity = normalize(document.getBranch().getCity().getName());
        if (hasHardCity) {
            if (contains(docCity, plan.getCity())) {
                return 0;
            }
            warnings.add("WRONG_CITY");
            return WRONG_CITY_PENALTY;
        }
        if (hasSoftCity && contains(docCity, plan.getPossibleCity())) {
            return -5;
        }
        return 0;
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
                document.getBusiness() != null ? normalize(document.getBusiness().getName()) : "",
                document.getBranch() != null ? normalize(document.getBranch().getName()) : "");
    }

    private Boolean contains(String value, String term) {
        if (value.isBlank() || term.isBlank()) {
            return false;
        }
        return value.contains(term);
    }

    private List<SearchDocumentType> resolvePlanDocumentTypes(SearchPlan plan) {
        return List.of(plan.getItemType());
    }

    private List<String> collectCategoryInputs(JsonNode intentStructure, SearchIntentStructureRequest request) {
        Set<String> terms = new LinkedHashSet<>();
        addTerm(terms, request.getSelectedCategory());
        addTerm(terms, intentStructure.path("item").path("primary_category").asText(""));
        addTerm(terms, intentStructure.path("item").path("item_type").asText(""));
        addTerm(terms, intentStructure.path("service").path("primary_category").asText(""));
        addTerm(terms, intentStructure.path("service").path("service_type").asText(""));
        return terms.stream().toList();
    }

    private List<String> resolveHardMatchTerms(JsonNode intentStructure, SearchIntentStructureRequest request) {
        Set<String> terms = new LinkedHashSet<>();
        String sourceText = normalize(request.getRawQuery());
        addFeaturePhrase(terms, request.getRawQuery());
        return terms.stream().toList();
    }

    private void addFeaturePhrase(Set<String> terms, String rawQuery) {
        String normalized = normalize(rawQuery);
        if (!searchTermEnricher.isKnownCommercialType(normalized) && normalized.split("\\s+").length > 1) {
            addTerm(terms, removePriceWording(normalized));
        }
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
        addTerm(terms, intentStructure.path("item").path("normalized_item_name").asText(""));
        addTerm(terms, intentStructure.path("item").path("item_type").asText(""));
        addTerm(terms, intentStructure.path("service").path("service_type").asText(""));
        addArrayTerms(terms, intentStructure.path("semantic").path("search_keywords"));
        return terms.stream().toList();
    }

    private List<String> resolveExpandedTerms(JsonNode intentStructure) {
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

    private BigDecimal resolvePossibleMaxPrice(SearchIntentStructureRequest request) {
        if (request.getExplicitMaxPrice() != null) {
            return null;
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

    private BigDecimal normalizePackageGrams(String rawNumber, String rawUnit) {
        BigDecimal value = new BigDecimal(rawNumber.replace(" ", "").replace(",", "."));
        String unit = normalize(rawUnit);
        if (List.of("кг", "kg", "килограмм", "килограмма", "килограммов").contains(unit)) {
            return value.multiply(BigDecimal.valueOf(1000L));
        }
        return value;
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
                searchQueryAliasRepository.findByAliasValue(normalized)
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

    private SearchCardResponse toV2Card(
            ScoredSearchDocument scored,
            Map<UUID, BusinessProfileDto> businessProfiles,
            String language) {
        SearchDocument document = scored.getDocument();
        UUID businessId = document.getBusiness() == null ? null : document.getBusiness().getId();
        BusinessProfileDto brandProfile = businessId == null ? null : businessProfiles.get(businessId);
        return SearchCardResponse.builder()
                .component(component(document.getDocumentType().name()))
                .resultId(document.getAggregateId())
                .businessId(document.getBusiness() != null ? document.getBusiness().getId() : null)
                .businessName(document.getBusiness() != null ? document.getBusiness().getName() : null)
                .resultType(document.getDocumentType().name())
                .brandColor(resolveBrandColor(brandProfile))
                .brandLogoUrl(brandProfile != null ? brandProfile.getLogoUrl() : null)
                .title(document.getTitle())
                .summary(document.getSummary())
                .categoryLabel(document.getCategoryLabel())
                .price(document.getPrice())
                .currency(document.getCurrency())
                .businessProfile(toBusinessProfile(brandProfile))
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
                .branchAddress(document.getBranch() != null ? document.getBranch().getAddress() : null)
                .branchCity(document.getBranch() != null && document.getBranch().getCity() != null
                        ? document.getBranch().getCity().getName() : null)
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

    private String resolveBrandColor(BusinessProfileDto profile) {
        if (profile == null || profile.getBrandColor() == null || profile.getBrandColor().isBlank()) {
            return DEFAULT_BRAND_COLOR;
        }
        return profile.getBrandColor();
    }

    private List<String> resolveBadges(BusinessProfileDto profile, SearchDocument document) {
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

    private Boolean hasOfficialLink(BusinessProfileDto profile) {
        return (profile.getWebsiteUrl() != null && !profile.getWebsiteUrl().isBlank())
                || (profile.getTelegramUrl() != null && !profile.getTelegramUrl().isBlank())
                || (profile.getInstagramUrl() != null && !profile.getInstagramUrl().isBlank());
    }

    private String component(String type) {
        return switch (type) {
            case "ITEM" -> "ItemCard";
            case "SERVICE" -> "ServiceCard";
            default -> throw new kz.ask.shared.error.InternalServerException(
                    kz.ask.shared.error.ErrorCode.SEARCH_PROJECTION_INVALID);
        };
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
        if ("ITEM_SEARCH".equals(requestType)) {
            attrNode = intentStructure.path("item").path("attributes");
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

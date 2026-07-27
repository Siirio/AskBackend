package kz.ask.search.basic.infrastructure.meilisearch;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.meilisearch.sdk.Client;
import com.meilisearch.sdk.Index;
import com.meilisearch.sdk.SearchRequest;
import com.meilisearch.sdk.exceptions.MeilisearchException;
import com.meilisearch.sdk.model.Searchable;
import com.meilisearch.sdk.model.Embedder;
import com.meilisearch.sdk.model.EmbedderSource;
import com.meilisearch.sdk.model.Hybrid;
import com.meilisearch.sdk.model.TaskInfo;
import com.meilisearch.sdk.model.SwapIndexesParams;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import kz.ask.search.basic.application.processor.SearchPlan;
import kz.ask.search.basic.domain.dto.MeilisearchIndexDocument;
import kz.ask.search.basic.domain.dto.SearchCandidateSetDto;
import kz.ask.shared.error.ErrorCode;
import kz.ask.shared.error.ExternalServiceException;
import kz.ask.shared.error.InternalServerException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class MeilisearchIndexGatewayImpl implements MeilisearchIndexGateway {

    private static final Integer DEFAULT_SEARCH_LIMIT = 200;
    private static final String FIELD_DOCUMENT_TYPE = "documentType";
    private static final String FIELD_PRICE = "price";
    private static final String FIELD_CITY = "city";
    private static final String FIELD_COUNTRY = "country";
    private static final String FIELD_CATEGORY_LABEL = "categoryLabel";
    private static final String FIELD_VERIFIED_ATTRIBUTES = "verifiedAttributes";
    private static final String FIELD_AI_ATTRIBUTES = "aiAttributes";
    private static final Double PURE_SEMANTIC_RATIO = 1.0;

    private final Client client;
    private final ObjectMapper objectMapper;
    private final String indexName;
    private final Boolean semanticEnabled;
    private final String semanticEmbedderName;
    private final String semanticEmbedderModel;
    private final Set<String> configuredIndexes = ConcurrentHashMap.newKeySet();
    private final Set<String> semanticIndexes = ConcurrentHashMap.newKeySet();
    private final MeilisearchFilterCompiler filterCompiler;
    private final ReciprocalRankFusionPolicy fusionPolicy;

    public MeilisearchIndexGatewayImpl(Client client, ObjectMapper objectMapper,
                                   MeilisearchFilterCompiler filterCompiler,
                                   ReciprocalRankFusionPolicy fusionPolicy,
                                   @Value("${ask.ai.meilisearch.index-name:search_documents_v1}") String indexName,
                                   @Value("${ask.ai.meilisearch.semantic-enabled:true}") Boolean semanticEnabled,
                                   @Value("${ask.ai.meilisearch.semantic-embedder-name:ask_multilingual}")
                                   String semanticEmbedderName,
                                   @Value("${ask.ai.meilisearch.semantic-embedder-model:sentence-transformers/paraphrase-multilingual-MiniLM-L12-v2}")
                                   String semanticEmbedderModel) {
        this.client = client;
        this.objectMapper = objectMapper;
        this.filterCompiler = filterCompiler;
        this.fusionPolicy = fusionPolicy;
        this.indexName = indexName;
        this.semanticEnabled = semanticEnabled;
        this.semanticEmbedderName = semanticEmbedderName;
        this.semanticEmbedderModel = semanticEmbedderModel;
    }

    @Override
    public void index(MeilisearchIndexDocument document) {
        try {
            Index index = ensureIndex();
            Map<String, Object> doc = toMeiliDocument(document);
            waitForTask(index, index.addDocuments(objectMapper.writeValueAsString(List.of(doc)), "id"));
        } catch (MeilisearchException | JsonProcessingException e) {
            throw new ExternalServiceException(ErrorCode.MEILISEARCH_INDEX_FAILED);
        }
    }

    @Override
    public void delete(UUID documentId) {
        try {
            Index index = ensureIndex();
            waitForTask(index, index.deleteDocument(documentId.toString()));
        } catch (MeilisearchException e) {
            throw new ExternalServiceException(ErrorCode.MEILISEARCH_DELETE_FAILED);
        }
    }

    @Override
    public void indexAll(List<MeilisearchIndexDocument> documents) {
        indexAll(indexName, documents);
    }

    @Override
    public String createRebuildIndex() {
        String rebuildIndex = indexName + "__rebuild__" + Instant.now().toEpochMilli();
        try {
            TaskInfo task = client.createIndex(rebuildIndex, "id");
            client.waitForTask(task.getTaskUid());
            configureSettings(client.getIndex(rebuildIndex));
            return rebuildIndex;
        } catch (MeilisearchException e) {
            throw new ExternalServiceException(ErrorCode.MEILISEARCH_INDEX_FAILED);
        }
    }

    @Override
    public void indexAll(String targetIndex, List<MeilisearchIndexDocument> documents) {
        if (documents.isEmpty()) {
            return;
        }
        try {
            Index index = ensureIndex(targetIndex);
            List<Map<String, Object>> docs = documents.stream()
                    .map(this::toMeiliDocument)
                    .toList();
            waitForTask(index, index.addDocuments(objectMapper.writeValueAsString(docs), "id"));
            log.info("Indexed {} documents into Meilisearch", documents.size());
        } catch (MeilisearchException | JsonProcessingException e) {
            throw new ExternalServiceException(ErrorCode.MEILISEARCH_INDEX_FAILED);
        }
    }

    @Override
    public void activateRebuildIndex(String rebuildIndex) {
        try {
            ensureIndex(indexName);
            TaskInfo swapTask = client.swapIndexes(new SwapIndexesParams[]{
                    new SwapIndexesParams().setIndexes(new String[]{indexName, rebuildIndex})
            });
            client.waitForTask(swapTask.getTaskUid());
            discardIndex(rebuildIndex);
        } catch (MeilisearchException e) {
            throw new ExternalServiceException(ErrorCode.MEILISEARCH_INDEX_FAILED);
        }
    }

    @Override
    public void discardIndex(String targetIndex) {
        try {
            TaskInfo task = client.deleteIndex(targetIndex);
            client.waitForTask(task.getTaskUid());
            configuredIndexes.remove(targetIndex);
        } catch (MeilisearchException e) {
            throw new ExternalServiceException(ErrorCode.MEILISEARCH_DELETE_FAILED);
        }
    }

    @Override
    public SearchCandidateSetDto search(SearchPlan plan, int limit) {
        try {
            Index index = ensureIndex();
            String exactQuery = normalize(plan.getRawQuery());
            String expandedQuery = expandedQuery(plan);
            List<UUID> exactLane = executeSearch(index, exactQuery, plan, limit);
            List<UUID> expandedLane = List.of();
            if (!expandedQuery.isBlank() && !expandedQuery.equals(exactQuery)) {
                expandedLane = executeSearch(index, expandedQuery, plan, limit);
            }
            List<UUID> semanticLane = executeSemanticSearch(index, plan, limit);
            return fusionPolicy.fuse(exactLane, expandedLane, semanticLane, limit);
        } catch (MeilisearchException | IllegalArgumentException e) {
            log.error("Meilisearch search failed: {}", e.getMessage());
            throw new ExternalServiceException(ErrorCode.MEILISEARCH_SEARCH_FAILED);
        }
    }

    private List<UUID> executeSemanticSearch(Index index, SearchPlan plan, int limit) {
        if (!semanticEnabled || !semanticIndexes.contains(index.getUid())) {
            return List.of();
        }
        String query = normalize(plan.getSemanticQuery());
        if (query.isBlank()) {
            return List.of();
        }
        try {
            SearchRequest request = buildSearchRequest(query, plan, limit)
                    .setHybrid(Hybrid.builder()
                            .semanticRatio(PURE_SEMANTIC_RATIO)
                            .embedder(semanticEmbedderName)
                            .build());
            Searchable result = index.search(request);
            return result.getHits().stream()
                    .map(hit -> hit.get("id"))
                    .filter(java.util.Objects::nonNull)
                    .map(value -> UUID.fromString(value.toString().replace("\"", "")))
                    .toList();
        } catch (MeilisearchException | IllegalArgumentException failure) {
            log.warn("Meilisearch semantic lane is unavailable: {}", failure.getMessage());
            return List.of();
        }
    }

    private List<UUID> executeSearch(Index index, String query, SearchPlan plan, int limit)
            throws MeilisearchException {
        if (query.isBlank()) {
            return List.of();
        }
        Searchable result = index.search(buildSearchRequest(query, plan, limit));
        return result.getHits().stream()
                .map(hit -> hit.get("id"))
                .filter(java.util.Objects::nonNull)
                .map(value -> UUID.fromString(value.toString().replace("\"", "")))
                .toList();
    }

    private SearchRequest buildSearchRequest(String query, SearchPlan plan, int limit) {
        String filter = filterCompiler.compile(plan);

        SearchRequest request = new SearchRequest(query)
                .setLimit(Math.min(limit, DEFAULT_SEARCH_LIMIT));

        if (!filter.isBlank()) {
            request.setFilter(new String[]{filter});
        }

        if ("price_asc".equals(plan.getSort())) {
            request.setSort(new String[]{FIELD_PRICE + ":asc"});
        }

        return request;
    }

    private String expandedQuery(SearchPlan plan) {
        java.util.stream.Stream<String> legacyTerms = java.util.stream.Stream.of(
                        plan.getExactTerms(), plan.getExpandedTerms(), plan.getAiSynonyms(),
                        plan.getRelatedTerms(), plan.getCategoryAliases(), plan.getConceptIds())
                .flatMap(List::stream)
                .filter(java.util.Objects::nonNull);
        java.util.stream.Stream<String> weightedTerms = plan.getWeightedTerms() == null
                ? java.util.stream.Stream.empty()
                : plan.getWeightedTerms().stream()
                        .sorted(java.util.Comparator.comparing(
                                kz.ask.search.basic.domain.dto.WeightedSearchTermDto::getWeight).reversed())
                        .map(kz.ask.search.basic.domain.dto.WeightedSearchTermDto::getValue);
        return java.util.stream.Stream.concat(weightedTerms, legacyTerms)
                .filter(term -> term != null && !term.isBlank())
                .map(String::trim)
                .distinct()
                .limit(20)
                .collect(java.util.stream.Collectors.joining(" "));
    }


    private Index ensureIndex() throws MeilisearchException {
        return ensureIndex(indexName);
    }

    private Index ensureIndex(String targetIndex) throws MeilisearchException {
        Index index;
        try {
            index = client.getIndex(targetIndex);
        } catch (MeilisearchException missingIndex) {
            TaskInfo task = client.createIndex(targetIndex, "id");
            client.waitForTask(task.getTaskUid());
            index = client.getIndex(targetIndex);
        }
        if (!configuredIndexes.contains(targetIndex)) {
            configureSettings(index);
        } else if (semanticEnabled && !semanticIndexes.contains(targetIndex)) {
            configureSemanticSettings(index);
        }
        return index;
    }

    private synchronized void configureSettings(Index index) throws MeilisearchException {
        if (configuredIndexes.contains(index.getUid())) {
            return;
        }
        waitForTask(index, index.updateSearchableAttributesSettings(new String[]{
                "title", "normalizedTitle", "brand", "categoryPath", "categoryLabel",
                "aliases", "conceptIds", "useCases", "semanticSummary", "embeddingText",
                "tokens", "aiSearchSummary", "summary",
                "businessName", "branchName"
        }));
        waitForTask(index, index.updateFilterableAttributesSettings(new String[]{
                FIELD_DOCUMENT_TYPE, FIELD_PRICE, FIELD_CITY,
                FIELD_COUNTRY,
                FIELD_CATEGORY_LABEL,
                FIELD_VERIFIED_ATTRIBUTES, FIELD_AI_ATTRIBUTES,
                "aggregateId", "currency", "availabilityStatus"
        }));
        waitForTask(index, index.updateSortableAttributesSettings(new String[]{
                FIELD_PRICE
        }));
        configureSemanticSettings(index);
        configuredIndexes.add(index.getUid());
        log.info("Meilisearch index '{}' settings configured", index.getUid());
    }

    private void configureSemanticSettings(Index index) {
        if (!semanticEnabled) {
            return;
        }
        try {
            Embedder current = index.getEmbeddersSettings().get(semanticEmbedderName);
            if (current == null || current.getSource() != EmbedderSource.HUGGING_FACE
                    || !semanticEmbedderModel.equals(current.getModel())) {
                Embedder embedder = new Embedder()
                        .setSource(EmbedderSource.HUGGING_FACE)
                        .setModel(semanticEmbedderModel)
                        .setDocumentTemplate("{{doc.embeddingText}}");
                waitForTask(index, index.updateEmbeddersSettings(Map.of(
                        semanticEmbedderName, embedder)));
            }
            semanticIndexes.add(index.getUid());
        } catch (MeilisearchException failure) {
            log.warn("Meilisearch semantic embedder is unavailable for index '{}': {}",
                    index.getUid(), failure.getMessage());
        }
    }

    private Map<String, Object> toMeiliDocument(MeilisearchIndexDocument doc) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", doc.getId());
        map.put("aggregateId", doc.getAggregateId());
        map.put("title", doc.getTitle());
        map.put("normalizedTitle", nullToEmpty(doc.getNormalizedTitle()));
        map.put("summary", nullToEmpty(doc.getSummary()));
        map.put("aiSearchSummary", nullToEmpty(doc.getAiSearchSummary()));
        map.put("aliases", nullToEmpty(doc.getAliases()));
        map.put("conceptIds", doc.getConceptIds() != null ? doc.getConceptIds() : List.of());
        map.put("useCases", doc.getUseCases() != null ? doc.getUseCases() : List.of());
        map.put("semanticSummary", nullToEmpty(doc.getSemanticSummary()));
        map.put("embeddingText", nullToEmpty(doc.getEmbeddingText()));
        map.put("brand", nullToEmpty(doc.getBrand()));
        map.put("categoryPath", nullToEmpty(doc.getCategoryPath()));
        map.put("categoryLabel", normalize(doc.getCategoryLabel()));
        map.put("businessName", nullToEmpty(doc.getBusinessName()));
        map.put("branchName", nullToEmpty(doc.getBranchName()));
        map.put("tokens", doc.getTokens() != null ? doc.getTokens() : List.of());
        map.put(FIELD_PRICE, doc.getPrice() != null ? doc.getPrice().doubleValue() : null);
        map.put("currency", nullToEmpty(doc.getCurrency()));
        map.put("latitude", doc.getLatitude());
        map.put("longitude", doc.getLongitude());
        map.put(FIELD_CITY, normalize(doc.getCity()));
        map.put(FIELD_COUNTRY, normalize(doc.getCountry()));
        if (doc.getLatitude() != null && doc.getLongitude() != null) {
            map.put("_geo", Map.of(
                    "lat", doc.getLatitude().doubleValue(),
                    "lng", doc.getLongitude().doubleValue()));
        }
        if (doc.getDocumentType() == null) {
            throw new InternalServerException(ErrorCode.SEARCH_PROJECTION_INVALID);
        }
        map.put(FIELD_DOCUMENT_TYPE, doc.getDocumentType());
        map.put(FIELD_VERIFIED_ATTRIBUTES, doc.getVerifiedAttributes() != null
                ? new HashMap<>(doc.getVerifiedAttributes()) : new HashMap<>());
        map.put(FIELD_AI_ATTRIBUTES, doc.getAiAttributes() != null
                ? new HashMap<>(doc.getAiAttributes()) : new HashMap<>());
        map.put("availabilityStatus", nullToEmpty(doc.getAvailabilityStatus()));
        map.put("projectionVersion", doc.getProjectionVersion());
        return map;
    }

    private void waitForTask(Index index, TaskInfo task) throws MeilisearchException {
        index.waitForTask(task.getTaskUid());
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

}

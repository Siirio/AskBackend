package kz.ask.search.basic.infrastructure.meilisearch;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.meilisearch.sdk.Client;
import com.meilisearch.sdk.Index;
import com.meilisearch.sdk.SearchRequest;
import com.meilisearch.sdk.exceptions.MeilisearchException;
import com.meilisearch.sdk.model.Embedder;
import com.meilisearch.sdk.model.EmbedderSource;
import com.meilisearch.sdk.model.Hybrid;
import com.meilisearch.sdk.model.Pagination;
import com.meilisearch.sdk.model.SearchResult;
import com.meilisearch.sdk.model.Searchable;
import com.meilisearch.sdk.model.SwapIndexesParams;
import com.meilisearch.sdk.model.TaskInfo;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import kz.ask.search.basic.application.processor.SearchPlan;
import kz.ask.search.basic.domain.dto.MeilisearchIndexDocument;
import kz.ask.search.basic.domain.dto.SearchHitDto;
import kz.ask.shared.error.ErrorCode;
import kz.ask.shared.error.ExternalServiceException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

@Slf4j
@Service
public class MeilisearchIndexGatewayImpl implements MeilisearchIndexGateway {

    private static final Double DEFAULT_SEMANTIC_RATIO = 0.5;
    private static final String FIELD_PRICE = "price";
    private static final String FIELD_DOCUMENT_TYPE = "documentType";
    private static final String FIELD_AGGREGATE_ID = "aggregateId";
    private static final String FIELD_BUSINESS_ID = "businessId";
    private static final String FIELD_BRANCH_ID = "branchId";

    private final Client client;
    private final ObjectMapper objectMapper;
    private final String indexName;
    private final Boolean semanticEnabled;
    private final String semanticEmbedderName;
    private final String semanticEmbedderModel;
    private final Set<String> configuredIndexes = ConcurrentHashMap.newKeySet();
    private final Set<String> semanticIndexes = ConcurrentHashMap.newKeySet();
    private final MeilisearchFilterCompiler filterCompiler;

    public MeilisearchIndexGatewayImpl(Client client, ObjectMapper objectMapper,
                                   MeilisearchFilterCompiler filterCompiler,
                                   @Value("${ask.ai.meilisearch.index-name:search_documents_v1}") String indexName,
                                   @Value("${ask.ai.meilisearch.semantic-enabled:true}") Boolean semanticEnabled,
                                   @Value("${ask.ai.meilisearch.semantic-embedder-name:ask_multilingual}")
                                   String semanticEmbedderName,
                                   @Value("${ask.ai.meilisearch.semantic-embedder-model:sentence-transformers/paraphrase-multilingual-MiniLM-L12-v2}")
                                   String semanticEmbedderModel) {
        this.client = client;
        this.objectMapper = objectMapper;
        this.filterCompiler = filterCompiler;
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
    public Page<SearchHitDto> search(SearchPlan plan, int page, int pageSize) {
        try {
            Index index = ensureIndex();
            long offset = (long) page * pageSize;
            if (offset > Integer.MAX_VALUE) {
                return new PageImpl<>(List.of(), PageRequest.of(page, pageSize), Integer.MAX_VALUE);
            }
            if ("unique_offers".equals(plan.getSort())
                    && plan.getActiveOfferAggregateIds() != null
                    && !plan.getActiveOfferAggregateIds().isEmpty()) {
                return searchUniqueOffersFirst(index, plan, page, pageSize, (int) offset);
            }
            SearchResult result = executeSearch(index, plan, (int) offset, pageSize, filterCompiler.compile(plan));
            return new PageImpl<>(toHits(result), PageRequest.of(page, pageSize), result.getEstimatedTotalHits());
        } catch (MeilisearchException e) {
            log.error("Meilisearch search failed: {}", e.getMessage());
            throw new ExternalServiceException(ErrorCode.MEILISEARCH_SEARCH_FAILED);
        }
    }

    @Override
    public Map<UUID, Integer> searchBusinessFacets(SearchPlan plan) {
        try {
            Index index = ensureIndex();
            SearchPlan facetPlan = plan.toBuilder().businessIds(null).build();
            SearchRequest request = buildSearchRequest(
                    facetPlan.getRawQuery(), facetPlan, 0, 0, filterCompiler.compile(facetPlan));
            request.setFacets(new String[]{FIELD_BUSINESS_ID});
            SearchResult result = (SearchResult) index.search(request);
            return toBusinessFacets(result.getFacetDistribution());
        } catch (MeilisearchException e) {
            log.error("Meilisearch business facet search failed: {}", e.getMessage());
            throw new ExternalServiceException(ErrorCode.MEILISEARCH_SEARCH_FAILED);
        }
    }

    private Page<SearchHitDto> searchUniqueOffersFirst(
            Index index, SearchPlan plan, int page, int pageSize, int offset) throws MeilisearchException {
        String baseFilter = filterCompiler.compile(plan);
        String activeIds = plan.getActiveOfferAggregateIds().stream()
                .distinct()
                .map(id -> "'" + id + "'")
                .collect(java.util.stream.Collectors.joining(", "));
        String activeFilter = combineFilters(baseFilter, FIELD_AGGREGATE_ID + " IN [" + activeIds + "]");
        String inactiveFilter = combineFilters(baseFilter, FIELD_AGGREGATE_ID + " NOT IN [" + activeIds + "]");

        SearchResult active = executeSearch(index, plan, offset, pageSize, activeFilter);
        int activeTotal = active.getEstimatedTotalHits();
        List<SearchHitDto> hits = new java.util.ArrayList<>();
        if (offset < activeTotal) {
            hits.addAll(toHits(active));
        }

        int remaining = pageSize - hits.size();
        int inactiveOffset = Math.max(0, offset - activeTotal);
        SearchResult inactive = executeSearch(index, plan, inactiveOffset, Math.max(remaining, 1), inactiveFilter);
        if (remaining > 0) {
            hits.addAll(toHits(inactive).stream().limit(remaining).toList());
        }
        long total = (long) activeTotal + inactive.getEstimatedTotalHits();
        return new PageImpl<>(hits, PageRequest.of(page, pageSize), total);
    }

    private SearchResult executeSearch(
            Index index, SearchPlan plan, int offset, int limit, String filter) throws MeilisearchException {
        Searchable searchable = index.search(buildSearchRequest(plan.getRawQuery(), plan, offset, limit, filter));
        return (SearchResult) searchable;
    }

    private List<SearchHitDto> toHits(SearchResult result) {
        return result.getHits().stream()
                .map(hit -> SearchHitDto.builder()
                        .aggregateId(parseId(hit.get("id")))
                        .build())
                .filter(hit -> hit.getAggregateId() != null)
                .toList();
    }

    private Map<UUID, Integer> toBusinessFacets(Object facetDistribution) {
        if (!(facetDistribution instanceof Map<?, ?> distributions)) {
            return Map.of();
        }
        Object businessDistribution = distributions.get(FIELD_BUSINESS_ID);
        if (!(businessDistribution instanceof Map<?, ?> businessCounts)) {
            return Map.of();
        }
        Map<UUID, Integer> facets = new HashMap<>();
        businessCounts.forEach((value, count) -> {
            UUID businessId = parseId(value);
            if (businessId != null && count instanceof Number number && number.intValue() > 0) {
                facets.put(businessId, number.intValue());
            }
        });
        return facets;
    }

    private String combineFilters(String baseFilter, String additionalFilter) {
        return baseFilter.isBlank() ? additionalFilter : "(" + baseFilter + ") AND " + additionalFilter;
    }

    private SearchRequest buildSearchRequest(String query, SearchPlan plan, int offset, int limit, String filter) {
        SearchRequest request = new SearchRequest(query)
                .setOffset(offset)
                .setLimit(limit);

        if (!filter.isBlank()) {
            request.setFilter(new String[]{filter});
        }

        if ("price_asc".equals(plan.getSort())) {
            request.setSort(new String[]{FIELD_PRICE + ":asc"});
        } else if ("price_desc".equals(plan.getSort())) {
            request.setSort(new String[]{FIELD_PRICE + ":desc"});
        } else if ("distance".equals(plan.getSort())
                && plan.getUserLatitude() != null && plan.getUserLongitude() != null) {
            request.setSort(new String[]{"_geoPoint(" + plan.getUserLatitude() + ", "
                    + plan.getUserLongitude() + "):asc"});
        }

        if (semanticEnabled && semanticIndexes.contains(indexName)) {
            request.setHybrid(Hybrid.builder()
                    .semanticRatio(DEFAULT_SEMANTIC_RATIO)
                    .embedder(semanticEmbedderName)
                    .build());
        }

        return request;
    }

    private UUID parseId(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return UUID.fromString(value.toString().replace("\"", ""));
        } catch (IllegalArgumentException e) {
            return null;
        }
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
                "embeddingText", "tokens", "summary",
                "businessName", "branchName"
        }));
        waitForTask(index, index.updateFilterableAttributesSettings(new String[]{
                FIELD_DOCUMENT_TYPE, FIELD_PRICE, "city",
                "country", "categoryLabel",
                FIELD_AGGREGATE_ID, FIELD_BUSINESS_ID, FIELD_BRANCH_ID,
                "currency", "availabilityStatus"
        }));
        waitForTask(index, index.updateSortableAttributesSettings(new String[]{
                FIELD_PRICE
        }));
        waitForTask(index, index.updatePaginationSettings(new Pagination(Integer.MAX_VALUE)));
        configureSemanticSettings(index);
        configuredIndexes.add(index.getUid());
        log.info("Meilisearch index '{}' settings configured", index.getUid());
    }

    private void configureSemanticSettings(Index index) {
        if (!semanticEnabled) {
            return;
        }
        try {
            Map<String, Embedder> embedders = index.getEmbeddersSettings();
            if (embedders == null) {
                Embedder embedder = new Embedder()
                        .setSource(EmbedderSource.HUGGING_FACE)
                        .setModel(semanticEmbedderModel)
                        .setDocumentTemplate("{{doc.embeddingText}}");
                waitForTask(index, index.updateEmbeddersSettings(Map.of(
                        semanticEmbedderName, embedder)));
                semanticIndexes.add(index.getUid());
                return;
            }
            Embedder current = embedders.get(semanticEmbedderName);
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
        map.put("brand", nullToEmpty(doc.getBrand()));
        map.put("categoryPath", nullToEmpty(doc.getCategoryPath()));
        map.put("categoryLabel", nullToEmpty(doc.getCategoryLabel()));
        map.put("businessName", nullToEmpty(doc.getBusinessName()));
        map.put("branchName", nullToEmpty(doc.getBranchName()));
        map.put(FIELD_BUSINESS_ID, doc.getBusinessId() == null ? null : doc.getBusinessId().toString());
        map.put(FIELD_BRANCH_ID, doc.getBranchId() == null ? null : doc.getBranchId().toString());
        map.put("embeddingText", nullToEmpty(doc.getEmbeddingText()));
        map.put("tokens", doc.getTokens() == null ? List.of() : doc.getTokens());
        map.put("price", doc.getPrice() != null ? doc.getPrice().doubleValue() : null);
        map.put("currency", nullToEmpty(doc.getCurrency()));
        map.put("city", nullToEmpty(doc.getCity()));
        map.put("country", nullToEmpty(doc.getCountry()));
        map.put("documentType", nullToEmpty(doc.getDocumentType()));
        map.put("verifiedAttributes", doc.getVerifiedAttributes() != null ? doc.getVerifiedAttributes() : Map.of());
        map.put("availabilityStatus", nullToEmpty(doc.getAvailabilityStatus()));
        map.put("latitude", doc.getLatitude() != null ? doc.getLatitude().doubleValue() : null);
        map.put("longitude", doc.getLongitude() != null ? doc.getLongitude().doubleValue() : null);
        if (doc.getLatitude() != null && doc.getLongitude() != null) {
            map.put("_geo", Map.of(
                    "lat", doc.getLatitude().doubleValue(),
                    "lng", doc.getLongitude().doubleValue()));
        }
        return map;
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private void waitForTask(Index index, TaskInfo task) throws MeilisearchException {
        try {
            client.waitForTask(task.getTaskUid());
        } catch (MeilisearchException e) {
            log.error("Meilisearch task {} failed for index '{}': {}", task.getTaskUid(),
                    index.getUid(), e.getMessage());
            throw e;
        }
    }
}

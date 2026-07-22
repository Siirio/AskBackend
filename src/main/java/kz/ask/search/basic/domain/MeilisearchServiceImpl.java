package kz.ask.search.basic.domain;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.meilisearch.sdk.Client;
import com.meilisearch.sdk.Index;
import com.meilisearch.sdk.SearchRequest;
import com.meilisearch.sdk.exceptions.MeilisearchException;
import com.meilisearch.sdk.model.Searchable;
import com.meilisearch.sdk.model.TaskInfo;
import com.meilisearch.sdk.model.SwapIndexesParams;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import kz.ask.search.basic.application.processor.SearchPlan;
import kz.ask.search.basic.domain.dto.MeilisearchIndexDocument;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class MeilisearchServiceImpl implements MeilisearchService {

    private static final Integer DEFAULT_SEARCH_LIMIT = 200;
    private static final String FIELD_DOCUMENT_TYPE = "documentType";
    private static final String FIELD_PRICE = "price";
    private static final String FIELD_CITY = "city";
    private static final String FIELD_VERIFIED_ATTRIBUTES = "verifiedAttributes";
    private static final String FIELD_AI_ATTRIBUTES = "aiAttributes";
    private static final String FIELD_SYNCED_AT = "syncedAt";

    private final Client client;
    private final ObjectMapper objectMapper;
    private final String indexName;
    private final Set<String> configuredIndexes = ConcurrentHashMap.newKeySet();

    public MeilisearchServiceImpl(Client client, ObjectMapper objectMapper,
                                   @Value("${ask.ai.meilisearch.index-name:search_documents_v1}") String indexName) {
        this.client = client;
        this.objectMapper = objectMapper;
        this.indexName = indexName;
    }

    @Override
    public void index(MeilisearchIndexDocument document) {
        try {
            Index index = ensureIndex();
            Map<String, Object> doc = toMeiliDocument(document);
            waitForTask(index, index.addDocuments(objectMapper.writeValueAsString(List.of(doc)), "id"));
        } catch (MeilisearchException | JsonProcessingException e) {
            throw new IllegalStateException("Failed to index document " + document.getId(), e);
        }
    }

    @Override
    public void delete(UUID documentId) {
        try {
            Index index = ensureIndex();
            waitForTask(index, index.deleteDocument(documentId.toString()));
        } catch (MeilisearchException e) {
            throw new IllegalStateException("Failed to delete document " + documentId, e);
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
            throw new IllegalStateException("Failed to create rebuild index " + rebuildIndex, e);
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
            throw new IllegalStateException("Failed to batch index " + documents.size() + " documents", e);
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
            throw new IllegalStateException("Failed to activate rebuild index " + rebuildIndex, e);
        }
    }

    @Override
    public void discardIndex(String targetIndex) {
        try {
            TaskInfo task = client.deleteIndex(targetIndex);
            client.waitForTask(task.getTaskUid());
            configuredIndexes.remove(targetIndex);
        } catch (MeilisearchException e) {
            throw new IllegalStateException("Failed to discard index " + targetIndex, e);
        }
    }

    @Override
    public List<UUID> search(SearchPlan plan, int limit) {
        try {
            Index index = ensureIndex();
            SearchRequest request = buildSearchRequest(plan, limit);
            Searchable result = index.search(request);
            return result.getHits().stream()
                    .map(hit -> {
                        Object idValue = hit.get("id");
                        if (idValue == null) {
                            return null;
                        }
                        return UUID.fromString(idValue.toString().replace("\"", ""));
                    })
                    .filter(id -> id != null)
                    .toList();
        } catch (Exception e) {
            log.error("Meilisearch search failed: {}", e.getMessage());
            throw new RuntimeException("Meilisearch search failed", e);
        }
    }

    private SearchRequest buildSearchRequest(SearchPlan plan, int limit) {
        String query = buildQueryString(plan);
        String filter = buildFilterString(plan);

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

    private String buildQueryString(SearchPlan plan) {
        String exactTerm = firstTerm(plan.getExactTerms());
        if (!exactTerm.isBlank()) {
            return exactTerm;
        }
        String semanticTerm = firstTerm(plan.getSemanticTerms());
        if (!semanticTerm.isBlank()) {
            return semanticTerm;
        }
        return firstTerm(plan.getHardMatchTerms());
    }

    private String firstTerm(List<String> terms) {
        return terms.stream()
                .filter(term -> term != null && !term.isBlank())
                .findFirst()
                .orElse("");
    }

    private String buildFilterString(SearchPlan plan) {
        List<String> filters = new ArrayList<>();

        if (plan.getItemType() != null) {
            filters.add(FIELD_DOCUMENT_TYPE + " = " + plan.getItemType().name());
        }

        if (plan.getMinPrice() != null) {
            filters.add(FIELD_PRICE + " >= " + plan.getMinPrice().toPlainString());
        }

        if (plan.getMaxPrice() != null) {
            filters.add(FIELD_PRICE + " <= " + plan.getMaxPrice().toPlainString());
        }

        if (plan.getCity() != null && !plan.getCity().isBlank()) {
            filters.add(FIELD_CITY + " = " + escapeValue(plan.getCity()));
        }

        if (plan.getNotWanted() != null && !plan.getNotWanted().isEmpty()) {
            appendNotWantedFilters(filters, plan);
        }

        return String.join(" AND ", filters);
    }

    private void appendNotWantedFilters(List<String> filters, SearchPlan plan) {
        for (String term : plan.getNotWanted()) {
            String normalized = normalize(term);
            if (normalized.isBlank()) {
                continue;
            }
            for (String key : AttributeKeys.ALL_KEYS) {
                filters.add(FIELD_VERIFIED_ATTRIBUTES + "." + key + " != " + escapeValue(normalized));
            }
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
        }
        return index;
    }

    private synchronized void configureSettings(Index index) throws MeilisearchException {
        if (configuredIndexes.contains(index.getUid())) {
            return;
        }
        waitForTask(index, index.updateSearchableAttributesSettings(new String[]{
                "title", "normalizedTitle", "brand", "categoryPath", "categoryLabel",
                "aliases", "tokens", "aiSearchSummary", "summary",
                "businessName", "branchName"
        }));
        waitForTask(index, index.updateFilterableAttributesSettings(new String[]{
                FIELD_DOCUMENT_TYPE, FIELD_PRICE, FIELD_CITY,
                FIELD_VERIFIED_ATTRIBUTES, FIELD_AI_ATTRIBUTES,
                "aggregateId", "currency", "availabilityStatus"
        }));
        waitForTask(index, index.updateSortableAttributesSettings(new String[]{
                FIELD_PRICE, FIELD_SYNCED_AT
        }));
        configuredIndexes.add(index.getUid());
        log.info("Meilisearch index '{}' settings configured", index.getUid());
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
        map.put("brand", nullToEmpty(doc.getBrand()));
        map.put("categoryPath", nullToEmpty(doc.getCategoryPath()));
        map.put("categoryLabel", nullToEmpty(doc.getCategoryLabel()));
        map.put("businessName", nullToEmpty(doc.getBusinessName()));
        map.put("branchName", nullToEmpty(doc.getBranchName()));
        map.put("tokens", doc.getTokens() != null ? doc.getTokens() : List.of());
        map.put(FIELD_PRICE, doc.getPrice() != null ? doc.getPrice().doubleValue() : 0);
        map.put("currency", nullToEmpty(doc.getCurrency()));
        map.put("latitude", doc.getLatitude());
        map.put("longitude", doc.getLongitude());
        map.put(FIELD_CITY, nullToEmpty(doc.getCity()));
        map.put(FIELD_DOCUMENT_TYPE, doc.getDocumentType() != null ? doc.getDocumentType() : "UNKNOWN");
        map.put(FIELD_VERIFIED_ATTRIBUTES, doc.getVerifiedAttributes() != null
                ? new HashMap<>(doc.getVerifiedAttributes()) : new HashMap<>());
        map.put(FIELD_AI_ATTRIBUTES, doc.getAiAttributes() != null
                ? new HashMap<>(doc.getAiAttributes()) : new HashMap<>());
        map.put("availabilityStatus", "UNKNOWN");
        map.put(FIELD_SYNCED_AT, doc.getSyncedAt() != null ? doc.getSyncedAt().toString() : Instant.now().toString());
        return map;
    }

    private void waitForTask(Index index, TaskInfo task) throws MeilisearchException {
        index.waitForTask(task.getTaskUid());
    }

    private String escapeValue(String value) {
        String escaped = value.replace("'", "\\'");
        return "'" + escaped + "'";
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

}

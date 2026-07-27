package kz.ask.search.search_query_enrichment.infrastructure.cache;

import com.fasterxml.jackson.databind.JsonNode;
import java.time.Instant;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import kz.ask.search.search_query_enrichment.api.dto.SearchIntentStructureRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class IntentStructureCache {

    private static final long TTL_SECONDS = 3600;

    private final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();

    public JsonNode get(SearchIntentStructureRequest request, String promptVersion,
                        String modelVersion, String schemaVersion) {
        String key = cacheKey(request, promptVersion, modelVersion, schemaVersion);
        CacheEntry entry = cache.get(key);
        if (entry == null) {
            return null;
        }
        if (entry.expiresAt.isBefore(Instant.now())) {
            cache.remove(key);
            return null;
        }
        log.debug("Cache hit for query: {}", key);
        return entry.data;
    }

    public void put(SearchIntentStructureRequest request, String promptVersion,
                    String modelVersion, String schemaVersion, JsonNode data) {
        String key = cacheKey(request, promptVersion, modelVersion, schemaVersion);
        cache.put(key, new CacheEntry(data, Instant.now().plusSeconds(TTL_SECONDS)));
        evictIfNeeded();
    }

    private void evictIfNeeded() {
        if (cache.size() < 1000) {
            return;
        }
        Instant now = Instant.now();
        Iterator<Map.Entry<String, CacheEntry>> it = cache.entrySet().iterator();
        while (it.hasNext()) {
            if (it.next().getValue().expiresAt.isBefore(now)) {
                it.remove();
            }
        }
    }

    private String normalizeKey(String rawQuery) {
        if (rawQuery == null) {
            return "";
        }
        return rawQuery.trim().toLowerCase(Locale.ROOT).replaceAll("\\s+", " ");
    }

    private String cacheKey(SearchIntentStructureRequest request, String promptVersion,
                            String modelVersion, String schemaVersion) {
        return String.join("|",
                normalizeKey(request.getRawQuery()),
                value(request.getSelectedMode()),
                value(request.getLanguage()),
                value(request.getCity()),
                value(request.getCountry()),
                value(request.getSelectedCategory()),
                value(request.getExplicitMinPrice()),
                value(request.getExplicitMaxPrice()),
                value(request.getOpenNow()),
                value(request.getRadiusMeters()),
                value(request.getSort()),
                request.getUserLocation() == null ? "" : value(request.getUserLocation().getLat()),
                request.getUserLocation() == null ? "" : value(request.getUserLocation().getLng()),
                value(promptVersion),
                value(modelVersion),
                value(schemaVersion));
    }

    private String value(Object value) {
        return value == null ? "" : value.toString().trim().toLowerCase(Locale.ROOT);
    }

    private record CacheEntry(JsonNode data, Instant expiresAt) {}
}

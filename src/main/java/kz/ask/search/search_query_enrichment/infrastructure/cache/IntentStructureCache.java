package kz.ask.search.search_query_enrichment.infrastructure.cache;

import java.time.Instant;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import kz.ask.search.basic.application.processor.SearchInterpretation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class IntentStructureCache {

    private static final long TTL_SECONDS = 3600;

    private final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();

    public SearchInterpretation getInterpretation(String queryKey, String promptVersion,
                                                   String modelVersion, String schemaVersion) {
        String key = cacheKey(queryKey, promptVersion, modelVersion, schemaVersion);
        CacheEntry entry = cache.get(key);
        if (entry == null) {
            return null;
        }
        if (entry.expiresAt.isBefore(Instant.now())) {
            cache.remove(key);
            return null;
        }
        log.debug("Cache hit for query: {}", key);
        return entry.interpretation;
    }

    public void putInterpretation(String queryKey, String promptVersion,
                                   String modelVersion, String schemaVersion,
                                   SearchInterpretation interpretation) {
        String key = cacheKey(queryKey, promptVersion, modelVersion, schemaVersion);
        cache.put(key, new CacheEntry(interpretation, Instant.now().plusSeconds(TTL_SECONDS)));
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

    private String cacheKey(String queryKey, String promptVersion,
                            String modelVersion, String schemaVersion) {
        return String.join("|",
                normalize(queryKey),
                normalize(promptVersion),
                normalize(modelVersion),
                normalize(schemaVersion));
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    private record CacheEntry(SearchInterpretation interpretation, Instant expiresAt) {}
}

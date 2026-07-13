package kz.ask.search.infrastructure.cache;

import com.fasterxml.jackson.databind.JsonNode;
import java.time.Instant;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class IntentStructureCache {

    private static final long TTL_SECONDS = 3600;

    private final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();

    public JsonNode get(String rawQuery) {
        String key = normalizeKey(rawQuery);
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

    public void put(String rawQuery, JsonNode data) {
        String key = normalizeKey(rawQuery);
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

    private record CacheEntry(JsonNode data, Instant expiresAt) {}
}

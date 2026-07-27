package kz.ask.search.basic.domain;

import java.util.UUID;
import kz.ask.search.basic.domain.dto.SearchReindexBatch;
import kz.ask.search.basic.infrastructure.meilisearch.MeilisearchIndexGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchReindexServiceImpl implements SearchReindexService {

    private final SearchReindexBatchService batchService;
    private final MeilisearchIndexGateway meilisearchIndexGateway;

    @Value("${ask.search.reindex.batch-size:250}")
    private Integer batchSize;

    @Override
    public Long rebuildMeilisearch() {
        String rebuildIndex = meilisearchIndexGateway.createRebuildIndex();
        long indexed = 0;
        UUID cursor = null;
        try {
            boolean hasMore;
            do {
                SearchReindexBatch batch = batchService.read(cursor, batchSize);
                meilisearchIndexGateway.indexAll(rebuildIndex, batch.getDocuments());
                indexed += batch.getDocuments().size();
                cursor = batch.getNextCursor();
                hasMore = batch.getHasMore();
                log.info("Search reindex progress index={} documents={} cursor={}", rebuildIndex, indexed, cursor);
            } while (hasMore);
            meilisearchIndexGateway.activateRebuildIndex(rebuildIndex);
            log.info("Search reindex activated index={} documents={}", rebuildIndex, indexed);
            return indexed;
        } catch (RuntimeException failure) {
            discard(rebuildIndex);
            throw failure;
        }
    }

    private void discard(String rebuildIndex) {
        try {
            meilisearchIndexGateway.discardIndex(rebuildIndex);
        } catch (RuntimeException cleanupFailure) {
            log.warn("Failed to discard rebuild index {}: {}", rebuildIndex, cleanupFailure.getMessage());
        }
    }
}

package kz.ask.search.domain;

import java.util.UUID;
import kz.ask.search.domain.dto.SearchReindexBatch;

public interface SearchReindexBatchService {

    SearchReindexBatch read(UUID cursor, Integer batchSize);
}

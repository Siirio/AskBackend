package kz.ask.search.basic.domain;

import java.util.UUID;
import kz.ask.search.basic.domain.dto.SearchReindexBatch;

public interface SearchReindexBatchService {

    SearchReindexBatch read(UUID cursor, Integer batchSize);
}

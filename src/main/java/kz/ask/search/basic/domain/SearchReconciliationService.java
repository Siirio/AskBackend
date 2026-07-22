package kz.ask.search.basic.domain;

import java.util.UUID;
import kz.ask.search.basic.domain.dto.SearchReconciliationBatch;

public interface SearchReconciliationService {

    SearchReconciliationBatch reconcileProducts(UUID cursor, Integer batchSize, Boolean repair);

    SearchReconciliationBatch reconcileServices(UUID cursor, Integer batchSize, Boolean repair);

    Long activeProjectionCount();
}

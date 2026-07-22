package kz.ask.search.basic.infrastructure.scheduler;

import java.util.UUID;
import kz.ask.search.basic.domain.SearchReconciliationService;
import kz.ask.search.basic.domain.dto.SearchReconciliationBatch;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "ask.search.reconciliation.enabled", havingValue = "true")
public class SearchReconciliationScheduler {

    private final SearchReconciliationService reconciliationService;

    @Value("${ask.search.reconciliation.batch-size:250}")
    private Integer batchSize;

    @Value("${ask.search.reconciliation.repair:true}")
    private Boolean repair;

    private UUID productCursor;
    private UUID serviceCursor;
    private long scanned;
    private long mismatches;
    private long repairs;

    @Scheduled(fixedDelayString = "${ask.search.reconciliation.interval:PT30S}")
    public void reconcile() {
        if (productCursor != null || serviceCursor == null) {
            SearchReconciliationBatch batch = reconciliationService.reconcileProducts(productCursor, batchSize, repair);
            record(batch);
            productCursor = batch.getHasMore() ? batch.getNextCursor() : null;
            if (batch.getHasMore()) {
                return;
            }
            serviceCursor = new UUID(0, 0);
        }

        UUID cursor = serviceCursor.getMostSignificantBits() == 0 && serviceCursor.getLeastSignificantBits() == 0
                ? null
                : serviceCursor;
        SearchReconciliationBatch batch = reconciliationService.reconcileServices(cursor, batchSize, repair);
        record(batch);
        if (batch.getHasMore()) {
            serviceCursor = batch.getNextCursor();
            return;
        }
        log.info("Search reconciliation completed scanned={} mismatches={} repairs={} activeProjections={}",
                scanned, mismatches, repairs, reconciliationService.activeProjectionCount());
        serviceCursor = null;
        scanned = 0;
        mismatches = 0;
        repairs = 0;
    }

    private void record(SearchReconciliationBatch batch) {
        scanned += batch.getScanned();
        mismatches += batch.getMismatches();
        repairs += batch.getRepairsQueued();
    }
}

package kz.ask.importing.application;

import lombok.RequiredArgsConstructor;
import org.springframework.transaction.support.TransactionSynchronization;

@RequiredArgsConstructor
public class ItemImportTransactionSynchronization implements TransactionSynchronization {

    private final Runnable committed;
    private final Runnable rolledBack;

    @Override
    public void afterCompletion(int status) {
        if (status == STATUS_COMMITTED) {
            committed.run();
        } else {
            rolledBack.run();
        }
    }
}

package kz.ask.chat.infrastructure;

import kz.ask.chat.domain.event.ChatFilesDeletionRequested;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class ChatFilesDeletionListener {

    private final ChatFileStorage chatFileStorage;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void delete(ChatFilesDeletionRequested event) {
        event.getStoredNames().forEach(chatFileStorage::delete);
    }
}

package kz.ask.chat.domain.repository;

import java.util.List;
import java.util.UUID;
import kz.ask.chat.domain.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, UUID> {

    List<ChatMessage> findByConversationIdOrderByCreatedAtAsc(UUID conversationId);

    boolean existsByConversationIdAndSenderType(UUID conversationId, String senderType);
}

package kz.ask.chat.domain.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import kz.ask.chat.domain.entity.ChatAttachment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatAttachmentRepository extends JpaRepository<ChatAttachment, UUID> {

    Optional<ChatAttachment> findByStoredName(String storedName);

    List<ChatAttachment> findByConversationId(UUID conversationId);

    void deleteByConversationId(UUID conversationId);
}

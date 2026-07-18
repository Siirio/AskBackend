package kz.ask.chat.domain;

import java.util.List;
import java.util.UUID;
import kz.ask.chat.api.dto.ChatAttachmentDto;
import kz.ask.chat.api.dto.ChatConversationDto;
import kz.ask.chat.api.dto.ChatMessageDto;
import kz.ask.chat.api.dto.SendMessageRequest;

public interface ChatService {

    ChatConversationDto startConversation(UUID customerId, UUID businessId, String subject);

    ChatConversationDto startSystemConversation(UUID businessId, String customerName);

    ChatMessageDto sendMessage(UUID conversationId, UUID senderUserId, String senderType, SendMessageRequest req);

    List<ChatConversationDto> listCustomerConversations(UUID customerId);

    List<ChatConversationDto> listBusinessActiveConversations(UUID businessId);

    List<ChatConversationDto> listPlatformConversations();

    List<ChatMessageDto> getMessages(UUID conversationId);

    ChatConversationDto getConversation(UUID conversationId);

    ChatConversationDto closeConversation(UUID conversationId);

    void markRead(UUID conversationId, String readerType);

    void notifyBusinesses(String customerName, List<UUID> businessIds);

    ChatConversationDto startManagedImportConversation(
            UUID ownerId,
            UUID businessId,
            UUID managedImportRequestId,
            String subject,
            String systemMessage);

    void deleteConversation(UUID conversationId);

    void deleteCustomerConversations(UUID customerId);

    void requireCustomerAccess(UUID conversationId, UUID userId);

    void requireBusinessAccess(UUID conversationId, UUID businessId);

    ChatAttachmentDto registerAttachment(UUID conversationId, UUID uploadedByUserId,
                                         String storedName, String originalName,
                                         String contentType, Long sizeBytes);

    ChatAttachmentDto findAttachmentByStoredName(String storedName);
}

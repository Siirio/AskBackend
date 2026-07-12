package kz.ask.chat.domain;

import java.util.List;
import java.util.UUID;
import kz.ask.chat.api.dto.ChatConversationDto;
import kz.ask.chat.api.dto.ChatMessageDto;
import kz.ask.chat.api.dto.SendMessageRequest;

public interface ChatService {

    ChatConversationDto startConversation(UUID customerId, UUID businessId, String subject);

    ChatConversationDto startSystemConversation(UUID businessId, String customerName);

    ChatMessageDto sendMessage(UUID conversationId, UUID senderUserId, String senderType, SendMessageRequest req);

    List<ChatConversationDto> listCustomerConversations(UUID customerId);

    List<ChatConversationDto> listBusinessActiveConversations(UUID businessId);

    List<ChatMessageDto> getMessages(UUID conversationId);

    void markRead(UUID conversationId, String readerType);

    void notifyBusinesses(String customerName, List<UUID> businessIds);
}

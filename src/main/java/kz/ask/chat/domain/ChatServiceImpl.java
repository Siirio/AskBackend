package kz.ask.chat.domain;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import kz.ask.chat.api.dto.ChatConversationDto;
import kz.ask.chat.api.dto.ChatMessageDto;
import kz.ask.chat.api.dto.SendMessageRequest;
import kz.ask.chat.domain.entity.ChatConversation;
import kz.ask.chat.domain.entity.ChatMessage;
import kz.ask.chat.domain.enums.MessageSenderType;
import kz.ask.chat.domain.repository.ChatConversationRepository;
import kz.ask.chat.domain.repository.ChatMessageRepository;
import kz.ask.shared.error.ErrorCode;
import kz.ask.shared.error.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private static final int MAX_CONVERSATIONS = 50;

    private final ChatConversationRepository conversationRepository;
    private final ChatMessageRepository messageRepository;

    @Override
    @Transactional
    public ChatConversationDto startConversation(UUID customerId, UUID businessId, String subject) {
        ChatConversation conv = new ChatConversation();
        conv.setBusinessId(businessId);
        conv.setCustomerId(customerId);
        conv.setSubject(subject);
        conv.setLastMessageAt(Instant.now());
        conv = conversationRepository.save(conv);
        return toConversationDto(conv);
    }

    @Override
    @Transactional
    public ChatConversationDto startSystemConversation(UUID businessId, String customerName) {
        ChatConversation conv = new ChatConversation();
        conv.setBusinessId(businessId);
        conv.setCustomerId(null);
        conv.setSubject(customerName);
        conv.setLastMessageAt(Instant.now());
        conv = conversationRepository.save(conv);
        return toConversationDto(conv);
    }

    @Override
    @Transactional
    public ChatMessageDto sendMessage(UUID conversationId, UUID senderUserId, String senderType, SendMessageRequest req) {
        ChatConversation conv = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.CONVERSATION_NOT_FOUND));

        if ("CUSTOMER".equals(senderType) && conv.getCustomerId() == null && senderUserId != null) {
            conv.setCustomerId(senderUserId);
        }
        conv.setLastMessageAt(Instant.now());

        if ("CUSTOMER".equals(senderType)) {
            conv.setBusinessUnreadCount(conv.getBusinessUnreadCount() + 1);
        } else {
            conv.setCustomerUnreadCount(conv.getCustomerUnreadCount() + 1);
        }
        conversationRepository.save(conv);

        ChatMessage msg = new ChatMessage();
        msg.setConversationId(conversationId);
        msg.setSenderType(MessageSenderType.valueOf(senderType));
        msg.setText(req.getText());
        msg = messageRepository.save(msg);

        return toMessageDto(msg);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChatConversationDto> listCustomerConversations(UUID customerId) {
        return conversationRepository.findByCustomerId(customerId, PageRequest.of(0, MAX_CONVERSATIONS))
                .stream()
                .map(this::toConversationDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChatConversationDto> listBusinessActiveConversations(UUID businessId) {
        return conversationRepository.findActiveByBusinessId(businessId, PageRequest.of(0, MAX_CONVERSATIONS))
                .stream()
                .map(this::toConversationDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChatMessageDto> getMessages(UUID conversationId) {
        if (!conversationRepository.existsById(conversationId)) {
            throw new NotFoundException(ErrorCode.CONVERSATION_NOT_FOUND);
        }
        return messageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId)
                .stream()
                .map(this::toMessageDto)
                .toList();
    }

    @Override
    @Transactional
    public void markRead(UUID conversationId, String readerType) {
        ChatConversation conv = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.CONVERSATION_NOT_FOUND));

        if ("CUSTOMER".equals(readerType)) {
            conv.setCustomerUnreadCount(0);
        } else {
            conv.setBusinessUnreadCount(0);
        }
        conversationRepository.save(conv);

        List<ChatMessage> messages = messageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId);
        MessageSenderType reader = MessageSenderType.valueOf(readerType);
        for (ChatMessage msg : messages) {
            if (msg.getReadAt() == null && msg.getSenderType() != reader) {
                msg.setReadAt(Instant.now());
                messageRepository.save(msg);
            }
        }
    }

    @Override
    @Transactional
    public void notifyBusinesses(String customerName, List<UUID> businessIds) {
        for (UUID businessId : businessIds) {
            startSystemConversation(businessId, customerName);
        }
    }

    private ChatConversationDto toConversationDto(ChatConversation conv) {
        return ChatConversationDto.builder()
                .conversationId(conv.getId())
                .businessId(conv.getBusinessId())
                .customerId(conv.getCustomerId())
                .subject(conv.getSubject())
                .customerUnreadCount(conv.getCustomerUnreadCount())
                .businessUnreadCount(conv.getBusinessUnreadCount())
                .lastMessageAt(conv.getLastMessageAt())
                .createdAt(conv.getCreatedAt())
                .build();
    }

    private ChatMessageDto toMessageDto(ChatMessage msg) {
        return ChatMessageDto.builder()
                .messageId(msg.getId())
                .conversationId(msg.getConversationId())
                .senderType(msg.getSenderType().name())
                .text(msg.getText())
                .readAt(msg.getReadAt())
                .createdAt(msg.getCreatedAt())
                .build();
    }
}

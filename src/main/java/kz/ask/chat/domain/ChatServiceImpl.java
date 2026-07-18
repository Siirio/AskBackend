package kz.ask.chat.domain;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import kz.ask.chat.api.dto.ChatAttachmentDto;
import kz.ask.chat.api.dto.ChatConversationDto;
import kz.ask.chat.api.dto.ChatMessageDto;
import kz.ask.chat.api.dto.SendMessageRequest;
import kz.ask.chat.domain.entity.ChatAttachment;
import kz.ask.chat.domain.entity.ChatConversation;
import kz.ask.chat.domain.entity.ChatMessage;
import kz.ask.chat.domain.event.ChatFilesDeletionRequested;
import kz.ask.chat.domain.enums.MessageSenderType;
import kz.ask.chat.domain.enums.ConversationStatus;
import kz.ask.chat.domain.enums.ConversationType;
import kz.ask.chat.domain.repository.ChatAttachmentRepository;
import kz.ask.chat.domain.repository.ChatConversationRepository;
import kz.ask.chat.domain.repository.ChatMessageRepository;
import kz.ask.identity.domain.entity.AppUser;
import kz.ask.identity.infrastructure.repository.AppUserRepository;
import kz.ask.shared.error.ErrorCode;
import kz.ask.shared.error.NotFoundException;
import kz.ask.shared.error.ForbiddenException;
import kz.ask.shared.error.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private static final int MAX_CONVERSATIONS = 50;
    private static final String FILE_URL_PREFIX = "/api/v1/chat/files/";

    private final ChatConversationRepository conversationRepository;
    private final ChatMessageRepository messageRepository;
    private final ChatAttachmentRepository attachmentRepository;
    private final AppUserRepository appUserRepository;
    private final ApplicationEventPublisher eventPublisher;

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
        if (conv.getConversationStatus() == ConversationStatus.CLOSED) {
            throw new ValidationException(ErrorCode.CONVERSATION_CLOSED);
        }

        if ("CUSTOMER".equals(senderType) && conv.getCustomerId() == null && senderUserId != null) {
            conv.setCustomerId(senderUserId);
        }
        if ("PLATFORM".equals(senderType) && conv.getConversationStatus() == ConversationStatus.PENDING) {
            conv.setConversationStatus(ConversationStatus.IN_CHAT);
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
        msg.setText(req.getText() == null ? "" : req.getText());
        msg.setAttachmentUrl(validateAttachmentUrl(conversationId, req.getAttachmentUrl()));
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
    public List<ChatConversationDto> listPlatformConversations() {
        return conversationRepository.findByConversationType(
                        ConversationType.MANAGED_IMPORT, PageRequest.of(0, MAX_CONVERSATIONS))
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

    @Override
    @Transactional
    public ChatConversationDto startManagedImportConversation(
            UUID ownerId,
            UUID businessId,
            UUID managedImportRequestId,
            String subject,
            String systemMessage) {
        ChatConversation conversation = new ChatConversation();
        conversation.setBusinessId(businessId);
        conversation.setCustomerId(ownerId);
        conversation.setSubject(subject);
        conversation.setConversationType(ConversationType.MANAGED_IMPORT);
        conversation.setConversationStatus(ConversationStatus.PENDING);
        conversation.setManagedImportRequestId(managedImportRequestId);
        conversation.setLastMessageAt(Instant.now());
        conversation = conversationRepository.save(conversation);

        ChatMessage message = new ChatMessage();
        message.setConversationId(conversation.getId());
        message.setSenderType(MessageSenderType.SYSTEM);
        message.setText(systemMessage);
        messageRepository.save(message);
        return toConversationDto(conversation);
    }

    @Override
    @Transactional
    public void deleteConversation(UUID conversationId) {
        List<String> storedNames = attachmentRepository.findByConversationId(conversationId)
                .stream()
                .map(attachment -> attachment.getStoredName())
                .toList();
        attachmentRepository.deleteByConversationId(conversationId);
        messageRepository.deleteByConversationId(conversationId);
        conversationRepository.deleteById(conversationId);
        eventPublisher.publishEvent(new ChatFilesDeletionRequested(storedNames));
    }

    @Override
    @Transactional
    public void deleteCustomerConversations(UUID customerId) {
        conversationRepository.findAllByCustomerId(customerId)
                .forEach(conversation -> deleteConversation(conversation.getId()));
    }

    @Override
    @Transactional(readOnly = true)
    public ChatConversationDto getConversation(UUID conversationId) {
        return conversationRepository.findById(conversationId)
                .map(this::toConversationDto)
                .orElseThrow(() -> new NotFoundException(ErrorCode.CONVERSATION_NOT_FOUND));
    }


    @Override
    @Transactional
    public ChatConversationDto closeConversation(UUID conversationId) {
        ChatConversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.CONVERSATION_NOT_FOUND));
        conversation.setConversationStatus(ConversationStatus.CLOSED);
        return toConversationDto(conversation);
    }

    @Override
    @Transactional
    public ChatAttachmentDto registerAttachment(UUID conversationId, UUID uploadedByUserId,
                                                String storedName, String originalName,
                                                String contentType, Long sizeBytes) {
        if (!conversationRepository.existsById(conversationId)) {
            throw new NotFoundException(ErrorCode.CONVERSATION_NOT_FOUND);
        }
        ChatAttachment attachment = new ChatAttachment();
        attachment.setConversationId(conversationId);
        attachment.setUploadedByUserId(uploadedByUserId);
        attachment.setStoredName(storedName);
        attachment.setOriginalName(originalName);
        attachment.setContentType(contentType);
        attachment.setSizeBytes(sizeBytes);
        return toAttachmentDto(attachmentRepository.save(attachment));
    }

    @Override
    @Transactional(readOnly = true)
    public ChatAttachmentDto findAttachmentByStoredName(String storedName) {
        return attachmentRepository.findByStoredName(storedName)
                .map(this::toAttachmentDto)
                .orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public void requireCustomerAccess(UUID conversationId, UUID userId) {
        ChatConversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.CONVERSATION_NOT_FOUND));
        if (!userId.equals(conversation.getCustomerId())) {
            throw new ForbiddenException(ErrorCode.ACCESS_DENIED);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public void requireBusinessAccess(UUID conversationId, UUID businessId) {
        ChatConversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.CONVERSATION_NOT_FOUND));
        if (!businessId.equals(conversation.getBusinessId())) {
            throw new ForbiddenException(ErrorCode.ACCESS_DENIED);
        }
    }

    private ChatConversationDto toConversationDto(ChatConversation conv) {
        String customerName = null;
        if (conv.getCustomerId() != null) {
            customerName = appUserRepository.findById(conv.getCustomerId())
                    .map(AppUser::getDisplayName)
                    .orElse(null);
        }
        if (customerName == null && conv.getSubject() != null && !conv.getSubject().isBlank()) {
            customerName = conv.getSubject();
        }
        return ChatConversationDto.builder()
                .conversationId(conv.getId())
                .businessId(conv.getBusinessId())
                .customerId(conv.getCustomerId())
                .customerName(customerName)
                .subject(conv.getSubject())
                .conversationType(conv.getConversationType().name())
                .conversationStatus(conv.getConversationStatus().name())
                .managedImportRequestId(conv.getManagedImportRequestId())
                .customerUnreadCount(conv.getCustomerUnreadCount())
                .businessUnreadCount(conv.getBusinessUnreadCount())
                .lastMessageAt(conv.getLastMessageAt())
                .createdAt(conv.getCreatedAt())
                .build();
    }

    private ChatAttachmentDto toAttachmentDto(ChatAttachment attachment) {
        return ChatAttachmentDto.builder()
                .id(attachment.getId())
                .conversationId(attachment.getConversationId())
                .storedName(attachment.getStoredName())
                .originalName(attachment.getOriginalName())
                .contentType(attachment.getContentType())
                .sizeBytes(attachment.getSizeBytes())
                .build();
    }

    private ChatMessageDto toMessageDto(ChatMessage msg) {
        return ChatMessageDto.builder()
                .messageId(msg.getId())
                .conversationId(msg.getConversationId())
                .senderType(msg.getSenderType().name())
                .text(msg.getText())
                .attachmentUrl(msg.getAttachmentUrl())
                .readAt(msg.getReadAt())
                .createdAt(msg.getCreatedAt())
                .build();
    }

    private String validateAttachmentUrl(UUID conversationId, String attachmentUrl) {
        if (attachmentUrl == null || attachmentUrl.isBlank()) {
            return null;
        }
        if (!attachmentUrl.startsWith(FILE_URL_PREFIX)) {
            throw new ValidationException(ErrorCode.FILE_INVALID);
        }
        String storedName = attachmentUrl.substring(FILE_URL_PREFIX.length());
        ChatAttachment attachment = attachmentRepository.findByStoredName(storedName)
                .orElseThrow(() -> new ValidationException(ErrorCode.FILE_INVALID));
        if (!conversationId.equals(attachment.getConversationId())) {
            throw new ValidationException(ErrorCode.FILE_INVALID);
        }
        return attachmentUrl;
    }
}

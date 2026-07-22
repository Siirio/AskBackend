package kz.ask.chat.application;

import java.util.List;
import java.util.UUID;
import kz.ask.chat.api.dto.ChatConversationDto;
import kz.ask.chat.api.dto.ChatMessageDto;
import kz.ask.chat.api.dto.SendMessageRequest;
import kz.ask.chat.domain.ChatService;
import kz.ask.chat.domain.enums.ConversationType;
import kz.ask.identity.infrastructure.security.AskPrincipal;
import kz.ask.managedimport.domain.ManagedImportService;
import kz.ask.platform.domain.PlatformMembershipService;
import kz.ask.platform.domain.dto.PlatformMembershipDto;
import kz.ask.identity.authorization.domain.enums.Permission;
import kz.ask.shared.error.ErrorCode;
import kz.ask.shared.error.ForbiddenException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class PlatformChatProcessor {

    private static final String PLATFORM_SENDER = "PLATFORM";

    private final ChatService chatService;
    private final PlatformMembershipService platformMembershipService;
    private final ManagedImportService managedImportService;

    @Transactional(readOnly = true)
    public List<ChatConversationDto> listConversations(AskPrincipal principal) {
        requireAnyPermission(principal,
                Permission.MANAGE_MANAGED_IMPORTS, Permission.MANAGE_SUPPORT_CHATS);
        return chatService.listPlatformConversations().stream()
                .filter(conversation -> conversation.getBusinessId() != null
                        && managedImportService.hasActiveGrant(
                                conversation.getBusinessId(), principal.getUserId()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ChatMessageDto> getMessages(AskPrincipal principal, UUID conversationId) {
        requireAnyPermission(principal,
                Permission.MANAGE_MANAGED_IMPORTS, Permission.MANAGE_SUPPORT_CHATS);
        requireManagedImportConversation(principal, conversationId);
        return chatService.getMessages(conversationId);
    }

    @Transactional
    public ChatMessageDto sendMessage(AskPrincipal principal, UUID conversationId, SendMessageRequest request) {
        requireAnyPermission(principal,
                Permission.MANAGE_MANAGED_IMPORTS, Permission.MANAGE_SUPPORT_CHATS);
        requireManagedImportConversation(principal, conversationId);
        return chatService.sendMessage(conversationId, principal.getUserId(), PLATFORM_SENDER, request);
    }

    @Transactional
    public void markRead(AskPrincipal principal, UUID conversationId) {
        requireAnyPermission(principal,
                Permission.MANAGE_MANAGED_IMPORTS, Permission.MANAGE_SUPPORT_CHATS);
        requireManagedImportConversation(principal, conversationId);
        chatService.markRead(conversationId, PLATFORM_SENDER);
    }

    @Transactional
    public ChatConversationDto closeConversation(AskPrincipal principal, UUID conversationId) {
        requireAnyPermission(principal, Permission.MANAGE_SUPPORT_CHATS);
        requireManagedImportConversation(principal, conversationId);
        return chatService.closeConversation(conversationId);
    }

    private void requireManagedImportConversation(AskPrincipal principal, UUID conversationId) {
        ChatConversationDto conversation = chatService.getConversation(conversationId);
        if (!ConversationType.MANAGED_IMPORT.name().equals(conversation.getConversationType())
                || conversation.getBusinessId() == null
                || !managedImportService.hasActiveGrant(
                        conversation.getBusinessId(), principal.getUserId())) {
            throw new ForbiddenException(ErrorCode.ACCESS_DENIED);
        }
    }

    private void requireAnyPermission(AskPrincipal principal, Permission... permissions) {
        PlatformMembershipDto membership = platformMembershipService.findActiveByUser(principal.getUserId());
        if (membership == null) {
            throw new ForbiddenException(ErrorCode.ACCESS_DENIED);
        }
        for (Permission permission : permissions) {
            if (membership.getPermissions().contains(permission)) {
                return;
            }
        }
        throw new ForbiddenException(ErrorCode.ACCESS_DENIED);
    }
}

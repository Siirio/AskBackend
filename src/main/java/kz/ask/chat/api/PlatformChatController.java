package kz.ask.chat.api;

import java.util.List;
import java.util.UUID;
import kz.ask.chat.api.dto.ChatConversationDto;
import kz.ask.chat.api.dto.ChatConversationListResponse;
import kz.ask.chat.api.dto.ChatMessageDto;
import kz.ask.chat.api.dto.ChatMessageListResponse;
import kz.ask.chat.api.dto.SendMessageRequest;
import kz.ask.chat.application.PlatformChatProcessor;
import kz.ask.identity.infrastructure.security.AskPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/platform/chat/conversations")
@RequiredArgsConstructor
public class PlatformChatController {

    private final PlatformChatProcessor platformChatProcessor;

    @GetMapping
    public ChatConversationListResponse listConversations(@AuthenticationPrincipal AskPrincipal principal) {
        List<ChatConversationDto> items = platformChatProcessor.listConversations(principal);
        return ChatConversationListResponse.builder().items(items).build();
    }

    @GetMapping("/{conversationId}/messages")
    public ChatMessageListResponse getMessages(@AuthenticationPrincipal AskPrincipal principal,
                                               @PathVariable UUID conversationId) {
        List<ChatMessageDto> items = platformChatProcessor.getMessages(principal, conversationId);
        return ChatMessageListResponse.builder().items(items).build();
    }

    @PostMapping("/{conversationId}/messages")
    public ChatMessageDto sendMessage(@AuthenticationPrincipal AskPrincipal principal,
                                      @PathVariable UUID conversationId,
                                      @RequestBody SendMessageRequest request) {
        return platformChatProcessor.sendMessage(principal, conversationId, request);
    }

    @PostMapping("/{conversationId}/read")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void markRead(@AuthenticationPrincipal AskPrincipal principal,
                         @PathVariable UUID conversationId) {
        platformChatProcessor.markRead(principal, conversationId);
    }

    @PostMapping("/{conversationId}/close")
    public ChatConversationDto closeConversation(@AuthenticationPrincipal AskPrincipal principal,
                                                 @PathVariable UUID conversationId) {
        return platformChatProcessor.closeConversation(principal, conversationId);
    }

}


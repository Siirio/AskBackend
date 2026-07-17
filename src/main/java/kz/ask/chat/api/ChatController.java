package kz.ask.chat.api;

import java.util.List;
import java.util.UUID;
import jakarta.validation.Valid;
import kz.ask.chat.api.dto.ChatConversationDto;
import kz.ask.chat.api.dto.ChatConversationListResponse;
import kz.ask.chat.api.dto.ChatMessageDto;
import kz.ask.chat.api.dto.ChatMessageListResponse;
import kz.ask.chat.api.dto.SendMessageRequest;
import kz.ask.chat.api.dto.SystemNotifyRequest;
import kz.ask.chat.domain.ChatService;
import kz.ask.identity.infrastructure.security.AskPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @PostMapping("/conversations")
    public ChatConversationDto startConversation(@AuthenticationPrincipal AskPrincipal principal,
                                                  @RequestParam UUID businessId,
                                                  @RequestParam String subject) {
        return chatService.startConversation(principal.getUserId(), businessId, subject);
    }

    @GetMapping("/conversations")
    public ChatConversationListResponse listConversations(@AuthenticationPrincipal AskPrincipal principal) {
        List<ChatConversationDto> items = chatService.listCustomerConversations(principal.getUserId());
        return ChatConversationListResponse.builder().items(items).build();
    }

    @GetMapping("/conversations/{conversationId}/messages")
    public ChatMessageListResponse getMessages(@AuthenticationPrincipal AskPrincipal principal,
                                               @PathVariable UUID conversationId) {
        chatService.requireCustomerAccess(conversationId, principal.getUserId());
        List<ChatMessageDto> items = chatService.getMessages(conversationId);
        return ChatMessageListResponse.builder().items(items).build();
    }

    @PostMapping("/conversations/{conversationId}/messages")
    public ChatMessageDto sendMessage(@AuthenticationPrincipal AskPrincipal principal,
                                       @PathVariable UUID conversationId,
                                       @RequestBody SendMessageRequest req) {
        chatService.requireCustomerAccess(conversationId, principal.getUserId());
        return chatService.sendMessage(conversationId, principal.getUserId(), "CUSTOMER", req);
    }

    @PostMapping("/conversations/{conversationId}/read")
    public void markRead(@AuthenticationPrincipal AskPrincipal principal,
                         @PathVariable UUID conversationId) {
        chatService.requireCustomerAccess(conversationId, principal.getUserId());
        chatService.markRead(conversationId, "CUSTOMER");
    }

    @PostMapping("/system-notify")
    public void notifyBusinesses(@AuthenticationPrincipal AskPrincipal principal,
                                 @Valid @RequestBody SystemNotifyRequest req) {
        String name = principal.getDisplayName() != null ? principal.getDisplayName() : "Пользователь";
        chatService.notifyBusinesses(name, req.getBusinessIds());
    }
}

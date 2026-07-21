package kz.ask.chat.api;

import java.util.List;
import java.util.UUID;
import kz.ask.business.domain.BusinessService;
import kz.ask.chat.api.dto.ChatConversationDto;
import kz.ask.chat.api.dto.ChatConversationListResponse;
import kz.ask.chat.api.dto.ChatMessageDto;
import kz.ask.chat.api.dto.ChatMessageListResponse;
import kz.ask.chat.api.dto.SendMessageRequest;
import kz.ask.chat.domain.ChatService;
import kz.ask.identity.infrastructure.security.AskPrincipal;
import kz.ask.shared.error.ErrorCode;
import kz.ask.shared.error.ForbiddenException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/business-admin/chats")
@RequiredArgsConstructor
public class BusinessChatController {

    private final ChatService chatService;
    private final BusinessService businessService;

    @GetMapping
    public ChatConversationListResponse listConversations(@AuthenticationPrincipal AskPrincipal principal,
                                                           @RequestParam UUID businessId) {
        requireBusinessMember(businessId, principal.getUserId());
        List<ChatConversationDto> items = chatService.listBusinessActiveConversations(businessId);
        return ChatConversationListResponse.builder().items(items).build();
    }

    @GetMapping("/{conversationId}/messages")
    public ChatMessageListResponse getMessages(@AuthenticationPrincipal AskPrincipal principal,
                                               @PathVariable UUID conversationId,
                                               @RequestParam UUID businessId) {
        requireBusinessMember(businessId, principal.getUserId());
        chatService.requireBusinessAccess(conversationId, businessId);
        List<ChatMessageDto> items = chatService.getMessages(conversationId);
        return ChatMessageListResponse.builder().items(items).build();
    }

    @PostMapping("/{conversationId}/messages")
    public ChatMessageDto sendMessage(@AuthenticationPrincipal AskPrincipal principal,
                                       @PathVariable UUID conversationId,
                                       @RequestParam UUID businessId,
                                       @RequestBody SendMessageRequest req) {
        requireBusinessMember(businessId, principal.getUserId());
        chatService.requireBusinessAccess(conversationId, businessId);
        return chatService.sendMessage(conversationId, null, "BUSINESS", req);
    }

    @PostMapping("/{conversationId}/read")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void markRead(@AuthenticationPrincipal AskPrincipal principal,
                         @PathVariable UUID conversationId,
                         @RequestParam UUID businessId) {
        requireBusinessMember(businessId, principal.getUserId());
        chatService.requireBusinessAccess(conversationId, businessId);
        chatService.markRead(conversationId, "BUSINESS");
    }

    private void requireBusinessMember(UUID businessId, UUID userId) {
        if (!businessService.isManagerOrAboveOfBusiness(businessId, userId)) {
            throw new ForbiddenException(ErrorCode.ACCESS_DENIED);
        }
    }
}

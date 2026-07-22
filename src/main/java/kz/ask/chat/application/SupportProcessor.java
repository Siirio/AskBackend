package kz.ask.chat.application;

import java.util.UUID;
import kz.ask.business.member.domain.BusinessMemberService;
import kz.ask.chat.api.dto.ChatConversationDto;
import kz.ask.chat.domain.ChatService;
import kz.ask.shared.error.ErrorCode;
import kz.ask.shared.error.ForbiddenException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SupportProcessor {

    private final ChatService chatService;
    private final BusinessMemberService businessMemberService;

    public ChatConversationDto open(UUID userId, UUID businessId) {
        if (businessId != null && businessMemberService.findByBusinessAndUser(businessId, userId) == null) {
            throw new ForbiddenException(ErrorCode.ACCESS_DENIED);
        }
        return chatService.getOrCreatePlatformSupportConversation(userId, businessId);
    }
}

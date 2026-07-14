package kz.ask.chat.api.dto;

import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatConversationDto {

    private UUID conversationId;
    private UUID businessId;
    private UUID customerId;
    private String customerName;
    private String subject;
    private Integer customerUnreadCount;
    private Integer businessUnreadCount;
    private Instant lastMessageAt;
    private Instant createdAt;
}

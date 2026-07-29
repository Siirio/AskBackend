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
public class ChatMessageDto {

    private UUID messageId;
    private UUID conversationId;
    private String senderType;
    private String text;
    private String attachmentUrl;
    private Instant readAt;
    private Instant createdAt;
}

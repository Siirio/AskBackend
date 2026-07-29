package kz.ask.chat.api.dto;

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
public class ChatAttachmentDto {

    private UUID id;
    private UUID conversationId;
    private String storedName;
    private String originalName;
    private String contentType;
    private Long sizeBytes;
}

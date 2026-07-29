package kz.ask.chat.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.util.UUID;
import kz.ask.shared.domain.entity.BaseUuidV7Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "chat_attachment")
@Getter
@Setter
@NoArgsConstructor
public class ChatAttachment extends BaseUuidV7Entity {

    @Column(nullable = false)
    private UUID conversationId;

    @Column(nullable = false, length = 128, unique = true)
    private String storedName;

    @Column(nullable = false, length = 512)
    private String originalName;

    @Column(nullable = false, length = 128)
    private String contentType;

    @Column(nullable = false)
    private Long sizeBytes;

    @Column(nullable = false)
    private UUID uploadedByUserId;
}

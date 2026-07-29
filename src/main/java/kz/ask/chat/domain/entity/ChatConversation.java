package kz.ask.chat.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import kz.ask.chat.domain.enums.ConversationStatus;
import kz.ask.chat.domain.enums.ConversationType;
import kz.ask.shared.domain.entity.BaseUuidV7Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "chat_conversation")
@Getter
@Setter
@NoArgsConstructor
public class ChatConversation extends BaseUuidV7Entity {

    private UUID businessId;

    private UUID customerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ConversationType conversationType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ConversationStatus conversationStatus;

    private UUID managedImportRequestId;

    @Column(nullable = false, length = 512)
    private String subject;

    @Column(nullable = false)
    private Integer customerUnreadCount;

    @Column(nullable = false)
    private Integer businessUnreadCount;

    private Instant lastMessageAt;
}

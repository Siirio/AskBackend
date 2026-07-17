package kz.ask.chat.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import kz.ask.chat.domain.enums.ConversationStatus;
import kz.ask.chat.domain.enums.ConversationType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "chat_conversation")
@Getter
@Setter
@NoArgsConstructor
public class ChatConversation {

    @Id
    private UUID id;

    @Column(name = "business_id", nullable = false)
    private UUID businessId;

    @Column(name = "customer_id")
    private UUID customerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "conversation_type", nullable = false)
    private ConversationType conversationType = ConversationType.GENERAL_SUPPORT;

    @Enumerated(EnumType.STRING)
    @Column(name = "conversation_status", nullable = false)
    private ConversationStatus conversationStatus = ConversationStatus.PENDING;

    @Column(name = "managed_import_request_id")
    private UUID managedImportRequestId;

    @Column(nullable = false, length = 512)
    private String subject;

    @Column(name = "customer_unread_count", nullable = false)
    private Integer customerUnreadCount = 0;

    @Column(name = "business_unread_count", nullable = false)
    private Integer businessUnreadCount = 0;

    @Column(name = "last_message_at")
    private Instant lastMessageAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        Instant now = Instant.now();
        if (createdAt == null) {
            createdAt = now;
        }
        if (updatedAt == null) {
            updatedAt = now;
        }
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }
}

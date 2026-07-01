package kz.ask.messaging.domain.entity;

import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import kz.ask.messaging.domain.enums.ConversationStatus;
import kz.ask.shared.domain.entity.BaseUuidV7Entity;

@Entity
@Getter
@Setter
@Table(name = "conversation")
public class Conversation extends BaseUuidV7Entity {

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ConversationStatus status;

    private String subject;
}

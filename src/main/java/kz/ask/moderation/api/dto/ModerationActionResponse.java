package kz.ask.moderation.api.dto;

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
public class ModerationActionResponse {

    private UUID id;
    private String targetType;
    private UUID targetId;
    private String action;
    private String moderationStatus;
    private String note;
    private Instant expiresAt;
    private Instant createdAt;
}

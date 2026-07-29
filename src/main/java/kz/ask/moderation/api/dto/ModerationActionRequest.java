package kz.ask.moderation.api.dto;

import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.UUID;
import kz.ask.platform.domain.enums.ModerationActionType;
import kz.ask.platform.domain.enums.ModerationTargetType;
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
public class ModerationActionRequest {

    @NotNull
    private ModerationTargetType targetType;

    @NotNull
    private UUID targetId;

    @NotNull
    private ModerationActionType action;

    private String reasonCode;

    private String note;

    private Instant expiresAt;
}

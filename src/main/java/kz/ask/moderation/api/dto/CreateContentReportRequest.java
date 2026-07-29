package kz.ask.moderation.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import kz.ask.platform.domain.enums.ModerationTargetType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateContentReportRequest {

    @NotNull
    private ModerationTargetType targetType;

    @NotNull
    private UUID targetId;

    @NotBlank
    private String reasonCode;

    private String details;
}

package kz.ask.moderation.api.dto;

import jakarta.validation.constraints.NotNull;
import kz.ask.business.domain.enums.BusinessModerationStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ModerateBusinessRequest {

    @NotNull
    private BusinessModerationStatus status;
}

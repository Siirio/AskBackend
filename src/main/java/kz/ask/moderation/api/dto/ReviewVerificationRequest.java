package kz.ask.moderation.api.dto;

import jakarta.validation.constraints.NotNull;
import kz.ask.business.domain.enums.VerificationStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReviewVerificationRequest {

    @NotNull
    private VerificationStatus status;

    private String comment;
}

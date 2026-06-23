package kz.ask.service.api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class HandleServiceRequestBody {
    @NotBlank
    private String action;
    private Instant confirmedStartAt;
    private Instant confirmedEndAt;
    private String providerNote;
}

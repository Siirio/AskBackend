package kz.ask.platform.api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeletePlatformAccountRequest {

    @NotBlank
    private String reason;
}

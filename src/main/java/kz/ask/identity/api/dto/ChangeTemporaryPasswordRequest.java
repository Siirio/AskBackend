package kz.ask.identity.api.dto;

import jakarta.validation.constraints.NotBlank;
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
public class ChangeTemporaryPasswordRequest {

    @NotBlank
    private String newPassword;

    @NotBlank
    private String passwordConfirmation;
}

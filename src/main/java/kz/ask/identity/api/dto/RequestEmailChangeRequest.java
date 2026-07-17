package kz.ask.identity.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RequestEmailChangeRequest {

    @NotBlank
    @Email
    private String newEmail;
}

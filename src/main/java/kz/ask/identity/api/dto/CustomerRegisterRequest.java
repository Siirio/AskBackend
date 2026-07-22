package kz.ask.identity.api.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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
public class CustomerRegisterRequest {

    private String displayName;
    @NotBlank
    @Email
    private String email;
    @NotBlank
    @Size(min = 8, max = 128)
    private String password;
    @NotBlank
    private String passwordConfirmation;
    private Boolean isRememberMe;
    private String countryCode = "KZ";
    private String locale = "ru";

    @AssertTrue(message = "Password and confirmation must match")
    public boolean passwordsMatch() {
        return password != null && password.equals(passwordConfirmation);
    }
}

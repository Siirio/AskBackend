package kz.ask.identity.api.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
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
public class CustomerLoginStartRequest {

    @Email
    private String email;
    private String phone;
    private Boolean rememberMe;

    @AssertTrue(message = "Exactly one of email or phone must be provided")
    public boolean hasSingleContact() {
        return (email != null && !email.isBlank()) ^ (phone != null && !phone.isBlank());
    }
}

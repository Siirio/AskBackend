package kz.ask.identity.api.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;

public class CustomerLoginStartRequest {

    @Email
    private String email;
    private String phone;
    private boolean rememberMe;

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public boolean isRememberMe() { return rememberMe; }
    public void setRememberMe(boolean rememberMe) { this.rememberMe = rememberMe; }

    @AssertTrue(message = "Exactly one of email or phone must be provided")
    public boolean hasSingleContact() {
        return (email != null && !email.isBlank()) ^ (phone != null && !phone.isBlank());
    }
}

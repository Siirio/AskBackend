package kz.ask.identity.api.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CustomerRegisterRequest {

    private String displayName;
    @Email
    private String email;
    private String phone;
    @NotBlank
    @Size(min = 8, max = 128)
    private String password;
    @NotBlank
    private String passwordConfirmation;
    private boolean acceptedUserAgreement;
    private boolean rememberMe;

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getPasswordConfirmation() { return passwordConfirmation; }
    public void setPasswordConfirmation(String passwordConfirmation) { this.passwordConfirmation = passwordConfirmation; }
    public boolean isAcceptedUserAgreement() { return acceptedUserAgreement; }
    public void setAcceptedUserAgreement(boolean acceptedUserAgreement) { this.acceptedUserAgreement = acceptedUserAgreement; }
    public boolean isRememberMe() { return rememberMe; }
    public void setRememberMe(boolean rememberMe) { this.rememberMe = rememberMe; }

    @AssertTrue(message = "Exactly one of email or phone must be provided")
    public boolean hasSingleContact() {
        return (email != null && !email.isBlank()) ^ (phone != null && !phone.isBlank());
    }

    @AssertTrue(message = "Password and confirmation must match")
    public boolean passwordsMatch() {
        return password != null && password.equals(passwordConfirmation);
    }

    @AssertTrue(message = "User agreement must be accepted")
    public boolean agreementAccepted() {
        return acceptedUserAgreement;
    }
}

package kz.ask.identity.api.dto;

import java.util.UUID;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class BusinessRegisterRequest {

    @Email
    private String email;
    private String phone;
    @NotBlank
    @Size(min = 8, max = 128)
    private String password;
    @NotBlank
    private String passwordConfirmation;
    @NotBlank
    private String businessName;
    @NotBlank
    private String branchName;
    private UUID branchCityId;
    private String branchAddress;
    private boolean onlineOnly;
    private boolean acceptedBusinessRules;
    private boolean rememberMe;

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getPasswordConfirmation() { return passwordConfirmation; }
    public void setPasswordConfirmation(String passwordConfirmation) { this.passwordConfirmation = passwordConfirmation; }
    public String getBusinessName() { return businessName; }
    public void setBusinessName(String businessName) { this.businessName = businessName; }
    public String getBranchName() { return branchName; }
    public void setBranchName(String branchName) { this.branchName = branchName; }
    public UUID getBranchCityId() { return branchCityId; }
    public void setBranchCityId(UUID branchCityId) { this.branchCityId = branchCityId; }
    public String getBranchAddress() { return branchAddress; }
    public void setBranchAddress(String branchAddress) { this.branchAddress = branchAddress; }
    public boolean isOnlineOnly() { return onlineOnly; }
    public void setOnlineOnly(boolean onlineOnly) { this.onlineOnly = onlineOnly; }
    public boolean isAcceptedBusinessRules() { return acceptedBusinessRules; }
    public void setAcceptedBusinessRules(boolean acceptedBusinessRules) { this.acceptedBusinessRules = acceptedBusinessRules; }
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

    @AssertTrue(message = "Business rules must be accepted")
    public boolean rulesAccepted() {
        return acceptedBusinessRules;
    }

    @AssertTrue(message = "Branch city and address must be provided for offline branch")
    public boolean hasOfflineBranchLocation() {
        return onlineOnly || (branchCityId != null && branchAddress != null && !branchAddress.isBlank());
    }
}

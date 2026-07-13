package kz.ask.identity.api.dto;

import java.util.UUID;
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
public class BusinessRegisterRequest {

    @NotBlank
    @Email
    private String email;
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
    private Boolean onlineOnly;
    private Boolean acceptedBusinessRules;
    private Boolean rememberMe;

    @AssertTrue(message = "Password and confirmation must match")
    public boolean passwordsMatch() {
        return password != null && password.equals(passwordConfirmation);
    }

    @AssertTrue(message = "Business rules must be accepted")
    public boolean rulesAccepted() {
        return acceptedBusinessRules != null && acceptedBusinessRules;
    }

    @AssertTrue(message = "Branch city and address must be provided for offline branch")
    public boolean hasOfflineBranchLocation() {
        return onlineOnly != null && onlineOnly || (branchCityId != null && branchAddress != null && !branchAddress.isBlank());
    }
}

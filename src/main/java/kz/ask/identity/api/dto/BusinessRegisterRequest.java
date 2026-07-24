package kz.ask.identity.api.dto;

import java.util.UUID;
import kz.ask.business.core.domain.enums.BusinessScope;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
    private String branchName;
    private UUID branchCityId;
    private String branchAddress;
    private Boolean onlineOnly;
    private UUID businessCategoryId;
    private String businessCategoryName;
    @NotNull
    private BusinessScope businessScope;
    private Boolean acceptedBusinessRules;
    private Boolean isRememberMe;
    private String countryCode;
    private String locale = "ru";

    @AssertTrue(message = "Password and confirmation must match")
    public boolean passwordsMatch() {
        return password != null && password.equals(passwordConfirmation);
    }

    @AssertTrue(message = "Business rules must be accepted")
    public boolean rulesAccepted() {
        return acceptedBusinessRules != null && acceptedBusinessRules;
    }

    @AssertTrue(message = "Business category is required")
    public boolean categoryProvided() {
        return businessCategoryId != null
                || businessCategoryName != null && !businessCategoryName.isBlank();
    }

}

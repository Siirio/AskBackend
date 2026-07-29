package kz.ask.business.onboarding.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;
import kz.ask.business.branch.api.dto.CreateBranchRequest;
import kz.ask.business.core.domain.enums.BusinessLegalForm;
import kz.ask.business.core.domain.enums.BusinessScope;
import kz.ask.business.profile.domain.enums.DeliveryCoverage;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SellerOnboardingRequest {

    private static final String OPTIONAL_HTTP_URL = "^(?:https?://\\S+)?$";
    private static final String LEGAL_IDENTIFIER = "^[0-9]{12}$";

    @NotBlank
    private String businessName;

    private UUID categoryId;

    private String categoryName;

    @NotBlank
    private String countryCode;

    @NotNull
    private BusinessLegalForm legalForm;

    private String legalIdentifier;

    private String legalName;

    @NotNull
    private CatalogSetupMode catalogSetupMode;

    @NotNull
    private BusinessScope businessScope;

    @NotNull
    private DeliveryCoverage deliveryCoverage;

    @Size(max = 50)
    private List<@Size(max = 120) String> deliveryCities;

    @NotNull
    private Boolean pickupAvailable;

    @Size(max = 50)
    private List<@Valid CreateBranchRequest> pickupBranches;

    @Pattern(regexp = OPTIONAL_HTTP_URL)
    private String twoGisUrl;

    @Pattern(regexp = OPTIONAL_HTTP_URL)
    private String kaspiUrl;

    @Pattern(regexp = OPTIONAL_HTTP_URL)
    private String ozonUrl;

    @Pattern(regexp = OPTIONAL_HTTP_URL)
    private String wildberriesUrl;

    @Pattern(regexp = OPTIONAL_HTTP_URL)
    private String websiteUrl;

    @Pattern(regexp = OPTIONAL_HTTP_URL)
    private String instagramUrl;

    @Pattern(regexp = OPTIONAL_HTTP_URL)
    private String telegramUrl;

    private String phone;

    private String corporateEmail;

    @AssertTrue(message = "A business category is required")
    public boolean isCategorySupplied() {
        return categoryId != null || categoryName != null && !categoryName.isBlank();
    }

    @AssertTrue(message = "A 12-digit legal identifier and legal name are required for Kazakhstan IP and TOO")
    public boolean isLegalDetailsValid() {
        if (legalForm == null || legalForm == BusinessLegalForm.NONE) {
            return true;
        }
        return legalIdentifier != null && legalIdentifier.matches(LEGAL_IDENTIFIER)
                && legalName != null && !legalName.isBlank();
    }

    @AssertTrue(message = "At least one valid business verification source is required")
    public boolean isVerificationSourceSupplied() {
        if (legalForm == null || legalForm != BusinessLegalForm.NONE) {
            return true;
        }
        return hasText(twoGisUrl) || hasText(kaspiUrl) || hasText(ozonUrl)
                || hasText(wildberriesUrl) || hasText(websiteUrl)
                || hasText(instagramUrl) || hasText(telegramUrl);
    }

    @AssertTrue(message = "At least one delivery city is required for selected city coverage")
    public boolean isDeliveryCoverageValid() {
        return deliveryCoverage == null
                || deliveryCoverage != DeliveryCoverage.SELECTED_CITIES
                || deliveryCities != null && deliveryCities.stream().anyMatch(this::hasText);
    }

    @AssertTrue(message = "At least one pickup branch is required when pickup is enabled")
    public boolean isPickupConfigurationValid() {
        return !Boolean.TRUE.equals(pickupAvailable)
                || pickupBranches != null && !pickupBranches.isEmpty();
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}

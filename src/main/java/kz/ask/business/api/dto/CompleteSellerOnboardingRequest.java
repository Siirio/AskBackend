package kz.ask.business.api.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import kz.ask.business.domain.enums.BusinessLegalForm;
import kz.ask.business.domain.enums.CatalogSetupMode;
import kz.ask.business.domain.enums.CatalogScope;
import kz.ask.business.domain.enums.CatalogSourceType;
import kz.ask.business.domain.enums.DeliveryScope;
import kz.ask.business.domain.enums.PreferredContactChannel;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CompleteSellerOnboardingRequest {

    @NotBlank
    private String businessName;

    private String countryCode = "KZ";

    @NotNull
    private BusinessLegalForm legalForm;

    private String legalIdentifier;

    private String legalName;

    @NotNull
    private PreferredContactChannel preferredContactChannel;

    @NotBlank
    private String preferredContactValue;

    private Boolean pickupAvailable = false;

    @NotNull
    private DeliveryScope deliveryScope;

    private Set<UUID> selectedCityIds = new LinkedHashSet<>();

    private String deliveryTermsRu;

    private String deliveryTermsKk;

    private String deliveryTermsEn;

    @NotNull
    private CatalogSetupMode catalogSetupMode;

    @NotNull
    private CatalogScope catalogScope;

    private Set<CatalogSourceType> catalogSources = new LinkedHashSet<>();

    private String sourceLinks;

    private String sourceNotes;

    private String locale = "ru";

    private Boolean legalAccepted;

    @AssertTrue(message = "Applicable legal documents must be accepted")
    public boolean legalAccepted() {
        return Boolean.TRUE.equals(legalAccepted);
    }
}

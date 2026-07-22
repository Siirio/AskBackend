package kz.ask.business.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import kz.ask.business.domain.enums.BusinessLegalForm;
import kz.ask.business.domain.enums.CatalogSetupMode;
import kz.ask.business.domain.enums.CatalogScope;
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
    private CatalogSetupMode catalogSetupMode;

    @NotNull
    private CatalogScope catalogScope;

    private String locale = "ru";

    private String binIin;

    private String twoGisUrl;

    private String kaspiUrl;

    private String ozonUrl;

    private String wildberriesUrl;

    private String websiteUrl;

    private String instagramUrl;

    private String telegramUrl;

    private String phone;

    private String corporateEmail;

}

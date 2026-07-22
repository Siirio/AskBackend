package kz.ask.business.domain;

import java.util.UUID;
import kz.ask.business.domain.dto.SellerOnboardingResult;
import kz.ask.business.domain.enums.BusinessLegalForm;
import kz.ask.business.domain.enums.CatalogSetupMode;
import kz.ask.business.domain.enums.CatalogScope;

public interface SellerOnboardingService {

    SellerOnboardingResult complete(
            UUID ownerId,
            String businessName,
            String countryCode,
            BusinessLegalForm legalForm,
            String legalIdentifier,
            String legalName,
            CatalogSetupMode catalogSetupMode,
            CatalogScope catalogScope,
            String binIin,
            String twoGisUrl,
            String kaspiUrl,
            String ozonUrl,
            String wildberriesUrl,
            String websiteUrl,
            String instagramUrl,
            String telegramUrl,
            String phone,
            String corporateEmail);
}

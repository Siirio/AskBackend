package kz.ask.business.domain;

import java.util.Set;
import java.util.UUID;
import kz.ask.business.domain.dto.SellerOnboardingResult;
import kz.ask.business.domain.enums.BusinessLegalForm;
import kz.ask.business.domain.enums.CatalogSetupMode;
import kz.ask.business.domain.enums.CatalogSourceType;
import kz.ask.business.domain.enums.DeliveryScope;
import kz.ask.business.domain.enums.PreferredContactChannel;

public interface SellerOnboardingService {

    SellerOnboardingResult complete(
            UUID ownerId,
            String businessName,
            String countryCode,
            BusinessLegalForm legalForm,
            String legalIdentifier,
            String legalName,
            PreferredContactChannel preferredContactChannel,
            String preferredContactValue,
            Boolean pickupAvailable,
            DeliveryScope deliveryScope,
            Set<UUID> selectedCityIds,
            String deliveryTermsRu,
            String deliveryTermsKk,
            String deliveryTermsEn,
            CatalogSetupMode catalogSetupMode,
            Set<CatalogSourceType> catalogSources,
            String catalogSourceLinks,
            String catalogSourceNotes);
}

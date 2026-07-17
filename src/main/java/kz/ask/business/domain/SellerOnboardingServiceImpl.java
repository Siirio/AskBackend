package kz.ask.business.domain;

import java.time.Duration;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import kz.ask.business.domain.dto.SellerOnboardingResult;
import kz.ask.business.domain.entity.Business;
import kz.ask.business.domain.entity.BusinessDeliveryProfile;
import kz.ask.business.domain.enums.BusinessLegalForm;
import kz.ask.business.domain.enums.CatalogSetupMode;
import kz.ask.business.domain.enums.CatalogStatus;
import kz.ask.business.domain.enums.CatalogSourceType;
import kz.ask.business.domain.enums.DeliveryScope;
import kz.ask.business.domain.enums.PreferredContactChannel;
import kz.ask.business.infrastructure.mapper.BusinessMapper;
import kz.ask.business.infrastructure.repository.BusinessDeliveryProfileRepository;
import kz.ask.business.infrastructure.repository.BusinessRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SellerOnboardingServiceImpl implements SellerOnboardingService {

    private final BusinessRepository businessRepository;
    private final BusinessDeliveryProfileRepository deliveryProfileRepository;
    private final BusinessMemberService businessMemberService;
    private final CityService cityService;
    private final BusinessMapper businessMapper;

    @Value("${business.catalog.deadline:P7D}")
    private Duration catalogDeadline;

    @Override
    @Transactional
    public SellerOnboardingResult complete(
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
            String catalogSourceNotes) {
        selectedCityIds.forEach(cityService::findById);
        Instant createdAt = Instant.now();
        Business business = businessMapper.toBusinessEntity(businessName);
        business.setCountryCode(countryCode);
        business.setLegalForm(legalForm);
        business.setLegalIdentifier(legalIdentifier);
        business.setLegalName(legalName);
        business.setPreferredContactChannel(preferredContactChannel);
        business.setPreferredContactValue(preferredContactValue);
        business.setCatalogSetupMode(catalogSetupMode);
        business.setCatalogSources(catalogSources);
        business.setCatalogSourceLinks(catalogSourceLinks);
        business.setCatalogSourceNotes(catalogSourceNotes);
        business.setCatalogDeadlineAt(createdAt.plus(catalogDeadline));
        business.setCatalogStatus(CatalogStatus.IN_PROGRESS);
        business = businessRepository.save(business);

        BusinessDeliveryProfile delivery = new BusinessDeliveryProfile();
        delivery.setBusiness(business);
        delivery.setPickupAvailable(Boolean.TRUE.equals(pickupAvailable));
        delivery.setDeliveryScope(deliveryScope);
        delivery.setSelectedCityIds(selectedCityIds);
        delivery.setTermsRu(deliveryTermsRu);
        delivery.setTermsKk(deliveryTermsKk);
        delivery.setTermsEn(deliveryTermsEn);
        deliveryProfileRepository.save(delivery);
        businessMemberService.createOwner(business.getId(), ownerId);

        return SellerOnboardingResult.builder()
                .businessId(business.getId())
                .catalogSetupMode(catalogSetupMode)
                .catalogDeadlineAt(business.getCatalogDeadlineAt())
                .build();
    }
}

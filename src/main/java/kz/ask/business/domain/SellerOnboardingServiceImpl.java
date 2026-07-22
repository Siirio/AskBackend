package kz.ask.business.domain;

import java.util.UUID;
import kz.ask.business.domain.dto.SellerOnboardingResult;
import kz.ask.business.domain.entity.Business;
import kz.ask.business.domain.entity.BusinessVerification;
import kz.ask.business.domain.enums.BusinessLegalForm;
import kz.ask.business.domain.enums.CatalogSetupMode;
import kz.ask.business.domain.enums.CatalogScope;
import kz.ask.business.domain.enums.VerificationStatus;
import kz.ask.business.infrastructure.mapper.BusinessMapper;
import kz.ask.business.infrastructure.repository.BusinessRepository;
import kz.ask.business.infrastructure.repository.BusinessVerificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SellerOnboardingServiceImpl implements SellerOnboardingService {

    private final BusinessRepository businessRepository;
    private final BusinessVerificationRepository verificationRepository;
    private final BusinessMemberService businessMemberService;
    private final CityService cityService;
    private final BusinessMapper businessMapper;

    @Override
    @Transactional
    public SellerOnboardingResult complete(
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
            String corporateEmail) {
        Business business = businessMapper.toBusinessEntity(businessName);
        business.setCountryCode(countryCode);
        business.setLegalForm(legalForm);
        business.setLegalIdentifier(legalIdentifier);
        business.setLegalName(legalName);
        business.setCatalogSetupMode(catalogSetupMode);
        business.setCatalogScope(catalogScope);
        business = businessRepository.save(business);

        BusinessVerification verification = new BusinessVerification();
        verification.setBusiness(business);
        verification.setStatus(VerificationStatus.PENDING);
        verification.setBinIin(blankToNull(binIin));
        verification.setTwoGisUrl(blankToNull(twoGisUrl));
        verification.setKaspiUrl(blankToNull(kaspiUrl));
        verification.setOzonUrl(blankToNull(ozonUrl));
        verification.setWildberriesUrl(blankToNull(wildberriesUrl));
        verification.setWebsiteUrl(blankToNull(websiteUrl));
        verification.setInstagramUrl(blankToNull(instagramUrl));
        verification.setTelegramUrl(blankToNull(telegramUrl));
        verification.setPhone(blankToNull(phone));
        verification.setCorporateEmail(blankToNull(corporateEmail));
        verificationRepository.save(verification);
        businessMemberService.createOwner(business.getId(), ownerId);

        return SellerOnboardingResult.builder()
                .businessId(business.getId())
                .catalogSetupMode(catalogSetupMode)
                .build();
    }

    private String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value.trim();
    }
}

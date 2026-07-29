package kz.ask.business.onboarding.application;

import java.util.List;
import java.util.UUID;
import kz.ask.business.branch.api.dto.CreateBranchRequest;
import kz.ask.business.branch.domain.BusinessBranchService;
import kz.ask.business.core.domain.BusinessService;
import kz.ask.business.core.domain.enums.BusinessLegalForm;
import kz.ask.business.onboarding.api.dto.CatalogSetupMode;
import kz.ask.business.onboarding.api.dto.SellerOnboardingRequest;
import kz.ask.business.onboarding.api.dto.SellerOnboardingResponse;
import kz.ask.business.verification.domain.BusinessVerificationService;
import kz.ask.business.verification.domain.dto.BusinessVerificationDto;
import kz.ask.business.verification.domain.enums.VerificationStatus;
import kz.ask.identity.infrastructure.security.AskPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class SellerOnboardingProcessor {

    private final BusinessService businessService;
    private final BusinessBranchService businessBranchService;
    private final BusinessVerificationService businessVerificationService;

    @Transactional
    public SellerOnboardingResponse onboard(AskPrincipal principal, SellerOnboardingRequest request) {
        var registration = businessService.onboard(
                principal.getUserId(), request.getBusinessName(), request.getCategoryId(),
                request.getCategoryName(), request.getBusinessScope(),
                request.getLegalForm(), request.getLegalIdentifier(), request.getLegalName(),
                request.getCountryCode(), request.getCorporateEmail(),
                request.getDeliveryCoverage(), request.getDeliveryCities(),
                request.getPickupAvailable(), !pickupBranches(request).isEmpty());
        pickupBranches(request).forEach(branch -> createBranch(registration.getBusiness().getId(), branch));
        businessVerificationService.create(registration.getBusiness().getId(),
                verification(request));
        return SellerOnboardingResponse.builder()
                .businessId(registration.getBusiness().getId())
                .catalogSetupMode(request.getCatalogSetupMode())
                .startRoute(request.getCatalogSetupMode() == CatalogSetupMode.ASK_MANAGED_IMPORT
                        ? "MANAGED_IMPORT" : "BUSINESS_CABINET")
                .build();
    }

    private List<CreateBranchRequest> pickupBranches(SellerOnboardingRequest request) {
        return request.getPickupBranches() == null ? List.of() : request.getPickupBranches();
    }

    private void createBranch(UUID businessId, CreateBranchRequest branch) {
        businessBranchService.create(
                businessId, branch.getCityId(), branch.getName(), branch.getAddress(),
                branch.getAddressDetails(), branch.getLatitude(), branch.getLongitude(),
                branch.getTimeZoneId(), branch.getWeeklyHours(), branch.getSpecialHours(), true);
    }

    private BusinessVerificationDto verification(SellerOnboardingRequest request) {
        return BusinessVerificationDto.builder()
                .status(request.getLegalForm() == BusinessLegalForm.NONE
                        ? VerificationStatus.PENDING : VerificationStatus.APPROVED)
                .twoGisUrl(blankToNull(request.getTwoGisUrl()))
                .kaspiUrl(blankToNull(request.getKaspiUrl()))
                .ozonUrl(blankToNull(request.getOzonUrl()))
                .wildberriesUrl(blankToNull(request.getWildberriesUrl()))
                .websiteUrl(blankToNull(request.getWebsiteUrl()))
                .instagramUrl(blankToNull(request.getInstagramUrl()))
                .telegramUrl(blankToNull(request.getTelegramUrl()))
                .phone(blankToNull(request.getPhone()))
                .corporateEmail(blankToNull(request.getCorporateEmail()))
                .build();
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}

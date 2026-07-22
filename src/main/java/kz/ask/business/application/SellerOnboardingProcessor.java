package kz.ask.business.application;

import java.util.Map;
import kz.ask.audit.domain.SignificantEventService;
import kz.ask.audit.domain.enums.SignificantEventType;
import kz.ask.business.api.dto.CompleteSellerOnboardingRequest;
import kz.ask.business.api.dto.SellerOnboardingResponse;
import kz.ask.business.domain.SellerOnboardingService;
import kz.ask.business.domain.dto.SellerOnboardingResult;
import kz.ask.business.domain.enums.BusinessLegalForm;
import kz.ask.business.domain.enums.CatalogSetupMode;
import kz.ask.identity.infrastructure.security.AskPrincipal;
import kz.ask.shared.error.ErrorCode;
import kz.ask.shared.error.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class SellerOnboardingProcessor {

    private final SellerOnboardingService sellerOnboardingService;
    private final SignificantEventService significantEventService;

    @Transactional
    public SellerOnboardingResponse complete(
            AskPrincipal principal,
            CompleteSellerOnboardingRequest request) {
        validate(principal, request);
        SellerOnboardingResult result = sellerOnboardingService.complete(
                principal.getUserId(),
                request.getBusinessName(),
                normalizeCountry(request.getCountryCode()),
                request.getLegalForm(),
                request.getLegalIdentifier(),
                request.getLegalName(),
                request.getCatalogSetupMode(),
                request.getCatalogScope(),
                request.getBinIin(),
                request.getTwoGisUrl(),
                request.getKaspiUrl(),
                request.getOzonUrl(),
                request.getWildberriesUrl(),
                request.getWebsiteUrl(),
                request.getInstagramUrl(),
                request.getTelegramUrl(),
                request.getPhone(),
                request.getCorporateEmail());
        significantEventService.record(principal.getUserId(),
                SignificantEventType.BUSINESS_CREATED,
                result.getBusinessId(), result.getBusinessId(), Map.of());
        return SellerOnboardingResponse.builder()
                .businessId(result.getBusinessId())
                .catalogSetupMode(result.getCatalogSetupMode())
                .startRoute(result.getCatalogSetupMode() == CatalogSetupMode.ASK_MANAGED_IMPORT
                        ? "MANAGED_IMPORT"
                        : "BUSINESS_CABINET")
                .build();
    }

    private void validate(AskPrincipal principal, CompleteSellerOnboardingRequest request) {
        if (request.getLegalForm() != BusinessLegalForm.NONE
                && (isBlank(request.getLegalIdentifier()) || isBlank(request.getLegalName()))) {
            throw new ValidationException(ErrorCode.SELLER_ONBOARDING_INVALID);
        }
    }

    private String normalizeCountry(String countryCode) {
        return isBlank(countryCode) ? "KZ" : countryCode;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}

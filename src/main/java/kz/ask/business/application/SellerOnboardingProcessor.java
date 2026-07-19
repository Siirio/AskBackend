package kz.ask.business.application;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import kz.ask.audit.domain.SignificantEventService;
import kz.ask.audit.domain.enums.SignificantEventType;
import kz.ask.business.api.dto.CompleteSellerOnboardingRequest;
import kz.ask.business.api.dto.SellerOnboardingResponse;
import kz.ask.business.domain.SellerOnboardingService;
import kz.ask.business.domain.dto.SellerOnboardingResult;
import kz.ask.business.domain.enums.BusinessLegalForm;
import kz.ask.business.domain.enums.CatalogSetupMode;
import kz.ask.business.domain.enums.CatalogSourceType;
import kz.ask.business.domain.enums.DeliveryScope;
import kz.ask.identity.infrastructure.security.AskPrincipal;
import kz.ask.legal.domain.LegalService;
import kz.ask.legal.domain.enums.LegalAcceptanceChannel;
import kz.ask.legal.domain.enums.LegalDocumentCode;
import kz.ask.managedimport.domain.ManagedImportService;
import kz.ask.managedimport.domain.dto.ManagedImportDto;
import kz.ask.shared.error.ErrorCode;
import kz.ask.shared.error.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class SellerOnboardingProcessor {

    private final SellerOnboardingService sellerOnboardingService;
    private final LegalService legalService;
    private final ManagedImportService managedImportService;
    private final SignificantEventService significantEventService;

    @Transactional
    public SellerOnboardingResponse complete(
            AskPrincipal principal,
            CompleteSellerOnboardingRequest request) {
        validate(request);
        Set<CatalogSourceType> sources = request.getCatalogSetupMode() == CatalogSetupMode.ASK_MANAGED_IMPORT
                ? new LinkedHashSet<>(request.getCatalogSources())
                : Set.of();
        SellerOnboardingResult result = sellerOnboardingService.complete(
                principal.getUserId(),
                request.getBusinessName(),
                normalizeCountry(request.getCountryCode()),
                request.getLegalForm(),
                request.getLegalIdentifier(),
                request.getLegalName(),
                request.getPreferredContactChannel(),
                request.getPreferredContactValue(),
                request.getPickupAvailable(),
                request.getDeliveryScope(),
                normalizedCities(request),
                request.getDeliveryTermsRu(),
                request.getDeliveryTermsKk(),
                request.getDeliveryTermsEn(),
                request.getCatalogSetupMode(),
                request.getCatalogScope(),
                sources,
                request.getSourceLinks(),
                request.getSourceNotes());
        significantEventService.record(principal.getUserId(),
                SignificantEventType.BUSINESS_CREATED,
                result.getBusinessId(), result.getBusinessId(), Map.of());
        legalService.acceptActiveDocuments(
                principal.getUserId(),
                legalCodes(request.getCatalogSetupMode()),
                normalizeCountry(request.getCountryCode()),
                normalizeLocale(request.getLocale()),
                LegalAcceptanceChannel.SELLER_ONBOARDING);
        ManagedImportDto managedImport = null;
        if (request.getCatalogSetupMode() == CatalogSetupMode.ASK_MANAGED_IMPORT) {
            managedImport = managedImportService.create(
                    result.getBusinessId(),
                    principal.getUserId(),
                    request.getCatalogScope(),
                    sources,
                    request.getPreferredContactChannel(),
                    request.getPreferredContactValue(),
                    request.getSourceLinks(),
                    request.getSourceNotes());
        }
        return SellerOnboardingResponse.builder()
                .businessId(result.getBusinessId())
                .catalogSetupMode(result.getCatalogSetupMode())
                .catalogDeadlineAt(result.getCatalogDeadlineAt())
                .conversationId(managedImport == null ? null : managedImport.getConversationId())
                .startRoute(result.getCatalogSetupMode() == CatalogSetupMode.ASK_MANAGED_IMPORT
                        ? "MANAGED_IMPORT"
                        : "BUSINESS_CABINET")
                .build();
    }

    private void validate(CompleteSellerOnboardingRequest request) {
        if (request.getLegalForm() != BusinessLegalForm.NONE
                && (isBlank(request.getLegalIdentifier()) || isBlank(request.getLegalName()))) {
            throw new ValidationException(ErrorCode.SELLER_ONBOARDING_INVALID);
        }
        if (request.getDeliveryScope() == DeliveryScope.SELECTED_CITIES
                && (request.getSelectedCityIds() == null || request.getSelectedCityIds().isEmpty())) {
            throw new ValidationException(ErrorCode.SELLER_ONBOARDING_INVALID);
        }
        if (request.getCatalogSetupMode() == CatalogSetupMode.ASK_MANAGED_IMPORT
                && (request.getCatalogSources() == null || request.getCatalogSources().isEmpty())) {
            throw new ValidationException(ErrorCode.SELLER_ONBOARDING_INVALID);
        }
    }

    private Set<java.util.UUID> normalizedCities(CompleteSellerOnboardingRequest request) {
        if (request.getDeliveryScope() != DeliveryScope.SELECTED_CITIES
                || request.getSelectedCityIds() == null) {
            return Set.of();
        }
        return new LinkedHashSet<>(request.getSelectedCityIds());
    }

    private Set<LegalDocumentCode> legalCodes(CatalogSetupMode mode) {
        Set<LegalDocumentCode> codes = new LinkedHashSet<>(Set.of(
                LegalDocumentCode.USER_TERMS,
                LegalDocumentCode.PRIVACY_POLICY,
                LegalDocumentCode.SELLER_TERMS,
                LegalDocumentCode.PROHIBITED_PRODUCTS_POLICY,
                LegalDocumentCode.CONTENT_POLICY));
        if (mode == CatalogSetupMode.ASK_MANAGED_IMPORT) {
            codes.add(LegalDocumentCode.MANAGED_IMPORT_TERMS);
        }
        return codes;
    }

    private String normalizeCountry(String countryCode) {
        return isBlank(countryCode) ? "KZ" : countryCode;
    }

    private String normalizeLocale(String locale) {
        return isBlank(locale) ? "ru" : locale;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}

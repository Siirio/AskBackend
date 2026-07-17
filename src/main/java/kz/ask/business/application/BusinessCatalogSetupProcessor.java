package kz.ask.business.application;

import java.util.Map;
import java.util.UUID;
import kz.ask.audit.domain.SignificantEventService;
import kz.ask.audit.domain.enums.SignificantEventType;
import kz.ask.business.api.dto.BusinessCatalogStatusResponse;
import kz.ask.business.domain.BusinessMemberService;
import kz.ask.business.domain.entity.Business;
import kz.ask.business.domain.enums.CatalogStatus;
import kz.ask.business.infrastructure.repository.BusinessRepository;
import kz.ask.identity.infrastructure.security.AskPrincipal;
import kz.ask.managedimport.domain.ManagedImportService;
import kz.ask.platform.domain.PlatformMembershipService;
import kz.ask.platform.domain.dto.PlatformMembershipDto;
import kz.ask.platform.domain.enums.PlatformPermission;
import kz.ask.search.domain.SearchVisibilityService;
import kz.ask.shared.error.ErrorCode;
import kz.ask.shared.error.ForbiddenException;
import kz.ask.shared.error.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class BusinessCatalogSetupProcessor {

    private final BusinessRepository businessRepository;
    private final BusinessMemberService businessMemberService;
    private final ManagedImportService managedImportService;
    private final PlatformMembershipService platformMembershipService;
    private final SearchVisibilityService searchVisibilityService;
    private final SignificantEventService significantEventService;

    @Transactional(readOnly = true)
    public BusinessCatalogStatusResponse status(AskPrincipal principal, UUID businessId) {
        requireAccess(principal, businessId);
        return toResponse(findBusiness(businessId));
    }

    @Transactional
    public BusinessCatalogStatusResponse complete(AskPrincipal principal, UUID businessId) {
        requireCompletionAccess(principal, businessId);
        Business business = findBusiness(businessId);
        business.setCatalogStatus(CatalogStatus.COMPLETED);
        searchVisibilityService.republishBusinessOffers(businessId);
        significantEventService.record(principal.getUserId(),
                SignificantEventType.CATALOG_PUBLISHED, businessId, businessId, Map.of());
        return toResponse(business);
    }

    private void requireAccess(AskPrincipal principal, UUID businessId) {
        if (businessMemberService.findByBusinessAndUser(
                businessId, principal.getUserId()) != null || hasPlatformGrant(principal, businessId)) {
            return;
        }
        throw new ForbiddenException(ErrorCode.ACCESS_DENIED);
    }

    private void requireCompletionAccess(AskPrincipal principal, UUID businessId) {
        if (businessMemberService.isManagerOrAboveOfBusiness(
                businessId, principal.getUserId()) || hasPlatformGrant(principal, businessId)) {
            return;
        }
        throw new ForbiddenException(ErrorCode.ACCESS_DENIED);
    }

    private boolean hasPlatformGrant(AskPrincipal principal, UUID businessId) {
        PlatformMembershipDto membership =
                platformMembershipService.findActiveByUser(principal.getUserId());
        return membership != null
                && membership.getPermissions().contains(
                        PlatformPermission.EDIT_CATALOG_DURING_IMPORT)
                && managedImportService.hasActiveGrant(businessId);
    }

    private Business findBusiness(UUID businessId) {
        return businessRepository.findById(businessId)
                .orElseThrow(() -> new NotFoundException(
                        ErrorCode.BUSINESS_NOT_FOUND, businessId));
    }

    private BusinessCatalogStatusResponse toResponse(Business business) {
        return BusinessCatalogStatusResponse.builder()
                .businessId(business.getId())
                .status(business.getCatalogStatus().name())
                .deadlineAt(business.getCatalogDeadlineAt())
                .build();
    }
}

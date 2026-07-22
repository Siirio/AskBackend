package kz.ask.offer.item.application;

import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;
import kz.ask.business.domain.BusinessMemberService;
import kz.ask.business.domain.dto.BusinessMemberDto;
import kz.ask.managedimport.domain.ManagedImportService;
import kz.ask.offer.item.api.dto.CatalogCapabilitiesResponse;
import kz.ask.offer.item.domain.enums.CatalogCapability;
import kz.ask.identity.infrastructure.security.AskPrincipal;
import kz.ask.platform.domain.PlatformMembershipService;
import kz.ask.platform.domain.dto.PlatformMembershipDto;
import kz.ask.platform.domain.enums.PlatformPermission;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class CatalogCapabilityProcessor {

    private final BusinessMemberService businessMemberService;
    private final PlatformMembershipService platformMembershipService;
    private final ManagedImportService managedImportService;

    @Transactional(readOnly = true)
    public CatalogCapabilitiesResponse capabilities(AskPrincipal principal, UUID businessId) {
        return CatalogCapabilitiesResponse.builder()
                .capabilities(capabilitiesFor(principal.getUserId(), businessId))
                .build();
    }

    private Set<CatalogCapability> capabilitiesFor(UUID userId, UUID businessId) {
        if (hasPlatformProductAccess(userId, businessId)) {
            return EnumSet.allOf(CatalogCapability.class);
        }
        BusinessMemberDto member = businessMemberService.findByBusinessAndUser(businessId, userId);
        if (member != null) {
            return EnumSet.of(CatalogCapability.MANUAL_PRODUCT_EDIT, CatalogCapability.EXCEL_IMPORT);
        }
        return EnumSet.noneOf(CatalogCapability.class);
    }

    private boolean hasPlatformProductAccess(UUID userId, UUID businessId) {
        if (!hasPlatformPermission(userId)) {
            return false;
        }
        var scope = managedImportService.activeScope(businessId, userId);
        return scope == kz.ask.business.domain.enums.CatalogScope.PRODUCTS
                || scope == kz.ask.business.domain.enums.CatalogScope.BOTH;
    }

    private boolean hasPlatformPermission(UUID userId) {
        PlatformMembershipDto membership = platformMembershipService.findActiveByUser(userId);
        return membership != null
                && membership.getPermissions().contains(PlatformPermission.EDIT_CATALOG_DURING_IMPORT);
    }
}

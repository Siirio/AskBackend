package kz.ask.catalog.domain;

import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;
import kz.ask.business.domain.BusinessMemberService;
import kz.ask.business.domain.dto.BusinessMemberDto;
import kz.ask.business.domain.enums.CatalogScope;
import kz.ask.catalog.domain.enums.CatalogCapability;
import kz.ask.managedimport.domain.ManagedImportService;
import kz.ask.platform.domain.PlatformMembershipService;
import kz.ask.platform.domain.dto.PlatformMembershipDto;
import kz.ask.platform.domain.enums.PlatformPermission;
import kz.ask.shared.domain.enums.RecordStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CatalogCapabilityServiceImpl implements CatalogCapabilityService {

    private final BusinessMemberService businessMemberService;
    private final PlatformMembershipService platformMembershipService;
    private final ManagedImportService managedImportService;

    @Override
    @Transactional(readOnly = true)
    public Set<CatalogCapability> capabilitiesFor(UUID userId, UUID businessId) {
        if (hasPlatformProductAccess(userId, businessId)) {
            return EnumSet.allOf(CatalogCapability.class);
        }
        BusinessMemberDto member = businessMemberService.findByBusinessAndUser(businessId, userId);
        if (member != null && RecordStatus.ACTIVE.name().equals(member.getStatus())) {
            return EnumSet.of(CatalogCapability.MANUAL_PRODUCT_EDIT, CatalogCapability.EXCEL_IMPORT);
        }
        return EnumSet.noneOf(CatalogCapability.class);
    }

    @Override
    @Transactional(readOnly = true)
    public Boolean hasPlatformCatalogAccess(UUID userId, UUID businessId) {
        return hasPlatformPermission(userId)
                && Boolean.TRUE.equals(managedImportService.hasActiveGrant(businessId, userId));
    }

    @Override
    @Transactional(readOnly = true)
    public Boolean hasPlatformProductAccess(UUID userId, UUID businessId) {
        if (!hasPlatformPermission(userId)) {
            return false;
        }
        CatalogScope scope = managedImportService.activeScope(businessId, userId);
        return scope == CatalogScope.PRODUCTS || scope == CatalogScope.BOTH;
    }

    @Override
    @Transactional(readOnly = true)
    public Boolean hasPlatformServiceAccess(UUID userId, UUID businessId) {
        if (!hasPlatformPermission(userId)) {
            return false;
        }
        CatalogScope scope = managedImportService.activeScope(businessId, userId);
        return scope == CatalogScope.SERVICES || scope == CatalogScope.BOTH;
    }

    private Boolean hasPlatformPermission(UUID userId) {
        PlatformMembershipDto membership = platformMembershipService.findActiveByUser(userId);
        return membership != null
                && membership.getPermissions().contains(PlatformPermission.EDIT_CATALOG_DURING_IMPORT);
    }
}

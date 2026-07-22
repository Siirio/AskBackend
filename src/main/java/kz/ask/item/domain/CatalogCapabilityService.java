package kz.ask.item.domain;

import java.util.Set;
import java.util.UUID;
import kz.ask.item.domain.enums.CatalogCapability;

public interface CatalogCapabilityService {

    Set<CatalogCapability> capabilitiesFor(UUID userId, UUID businessId);

    Boolean hasPlatformCatalogAccess(UUID userId, UUID businessId);

    Boolean hasPlatformProductAccess(UUID userId, UUID businessId);

    Boolean hasPlatformServiceAccess(UUID userId, UUID businessId);
}

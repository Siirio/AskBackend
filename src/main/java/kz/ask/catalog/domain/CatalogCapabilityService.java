package kz.ask.catalog.domain;

import java.util.Set;
import java.util.UUID;
import kz.ask.catalog.domain.enums.CatalogCapability;

public interface CatalogCapabilityService {

    Set<CatalogCapability> capabilitiesFor(UUID userId, UUID businessId);

    Boolean hasPlatformCatalogAccess(UUID userId, UUID businessId);
}

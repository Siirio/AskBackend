package kz.ask.item.application;

import java.util.UUID;
import kz.ask.item.api.dto.CatalogCapabilitiesResponse;
import kz.ask.item.domain.CatalogCapabilityService;
import kz.ask.identity.infrastructure.security.AskPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class CatalogCapabilityProcessor {

    private final CatalogCapabilityService catalogCapabilityService;

    @Transactional(readOnly = true)
    public CatalogCapabilitiesResponse capabilities(AskPrincipal principal, UUID businessId) {
        return CatalogCapabilitiesResponse.builder()
                .capabilities(catalogCapabilityService.capabilitiesFor(principal.getUserId(), businessId))
                .build();
    }
}

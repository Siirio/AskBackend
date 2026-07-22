package kz.ask.business.domain.dto;

import java.util.UUID;
import kz.ask.business.domain.enums.CatalogSetupMode;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SellerOnboardingResult {

    private UUID businessId;
    private CatalogSetupMode catalogSetupMode;
}

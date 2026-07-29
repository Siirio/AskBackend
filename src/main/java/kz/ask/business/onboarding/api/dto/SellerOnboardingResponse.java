package kz.ask.business.onboarding.api.dto;

import java.util.UUID;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class SellerOnboardingResponse {

    private UUID businessId;
    private CatalogSetupMode catalogSetupMode;
    private String startRoute;
}

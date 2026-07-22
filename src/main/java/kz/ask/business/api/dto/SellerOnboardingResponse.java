package kz.ask.business.api.dto;

import java.util.UUID;
import kz.ask.business.domain.enums.CatalogSetupMode;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SellerOnboardingResponse {

    private UUID businessId;
    private CatalogSetupMode catalogSetupMode;
    private UUID conversationId;
    private String startRoute;
}

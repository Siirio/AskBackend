package kz.ask.business.api.dto;

import java.time.Instant;
import java.util.UUID;
import kz.ask.business.domain.enums.CatalogSetupMode;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SellerOnboardingResponse {

    private UUID businessId;
    private CatalogSetupMode catalogSetupMode;
    private Instant catalogDeadlineAt;
    private String startRoute;
}

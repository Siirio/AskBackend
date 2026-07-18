package kz.ask.moderation.api.dto;

import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CatalogReviewBusinessResponse {

    private UUID businessId;
    private String businessName;
    private String catalogStatus;
}

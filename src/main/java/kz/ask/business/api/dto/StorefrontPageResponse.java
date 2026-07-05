package kz.ask.business.api.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StorefrontPageResponse {
    private UUID businessId;
    private BrandProfileResponse brandProfile;
    private List<StorefrontBlockResponse> blocks;
    private Instant publishedAt;
}

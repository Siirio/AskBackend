package kz.ask.moderation.api.dto;

import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProductModerationItemResponse {

    private UUID productId;
    private String productName;
    private UUID businessId;
    private String businessName;
    private String imageUrl;
    private Instant createdAt;
    private String moderationNote;
    private String moderationStatus;
}

package kz.ask.autodump.domain.dto;

import java.math.BigDecimal;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class DraftItemDto {

    private UUID id;
    private UUID importSessionId;
    private UUID aiJobId;
    private String itemType;
    private String status;
    private String title;
    private String normalizedTitle;
    private String categoryLabel;
    private String subcategoryLabel;
    private String description;
    private BigDecimal price;
    private String priceText;
    private String currency;
    private String brand;
    private String tagsJson;
    private String customAttributesJson;
    private String sourceReference;
    private String confidenceNotes;
    private Boolean needsReview;
    private String duplicateGroupKey;
    private UUID publishedProductOfferId;
    private UUID publishedServiceBranchOfferId;
}

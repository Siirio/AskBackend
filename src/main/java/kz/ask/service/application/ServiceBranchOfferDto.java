package kz.ask.service.application;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceBranchOfferDto {

    private UUID serviceOfferingId;
    private UUID serviceBranchOfferId;
    private Long searchVersion;
    private UUID businessId;
    private UUID branchId;
    private UUID categoryId;
    private String categoryLabel;
    private String name;
    private String description;
    private BigDecimal basePrice;
    private String scheduleText;
    private Boolean active;
    private String status;
    private String imageUrl;
    private Instant updatedAt;
}

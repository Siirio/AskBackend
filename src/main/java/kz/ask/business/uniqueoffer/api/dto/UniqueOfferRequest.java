package kz.ask.business.uniqueoffer.api.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import kz.ask.business.uniqueoffer.domain.enums.UniqueOfferStatus;
import kz.ask.business.uniqueoffer.domain.enums.UniqueOfferType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UniqueOfferRequest {

    @Size(max = 255)
    private String name;

    @Size(max = 2000)
    private String description;

    private Instant startDate;
    private Instant endDate;

    private UniqueOfferType type;

    private UniqueOfferStatus status;

    @Min(0)
    @Max(100)
    private Integer discountPercent;

    @DecimalMin(value = "0", inclusive = false)
    private java.math.BigDecimal discountAmount;

    private Boolean isActive;

    @Size(min = 3, max = 3)
    private String currency;

    @Size(max = 50)
    private List<String> tags;

    @Size(max = 500)
    private List<UUID> itemIds;

    @Size(max = 500)
    private List<UUID> serviceIds;

    @Size(max = 500)
    private List<UUID> branchIds;
}

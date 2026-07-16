package kz.ask.catalog.domain.dto;

import java.math.BigDecimal;
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
public class ProductOfferDto {
    private UUID id;
    private Long searchVersion;
    private UUID productId;
    private UUID branchId;
    private BigDecimal price;
    private Boolean enabled;
    private String status;
}

package kz.ask.business.branch.api.dto;

import java.math.BigDecimal;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BranchResponse {
    private UUID id;
    private UUID businessId;
    private UUID cityId;
    private String cityName;
    private String name;
    private String address;
    private String addressDetails;
    private Boolean isOnlineOnly;
    private BigDecimal latitude;
    private BigDecimal longitude;
}

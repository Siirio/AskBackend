package kz.ask.business.branch.api.dto;

import java.math.BigDecimal;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateBranchRequest {
    private String name;
    private String address;
    private String addressDetails;
    private UUID cityId;
    private Boolean isOnlineOnly;
    private BigDecimal latitude;
    private BigDecimal longitude;
}

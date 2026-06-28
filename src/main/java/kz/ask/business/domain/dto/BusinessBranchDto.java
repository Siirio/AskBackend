package kz.ask.business.domain.dto;

import java.math.BigDecimal;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class BusinessBranchDto {

    private UUID id;
    private UUID businessId;
    private UUID cityId;
    private String cityName;
    private String name;
    private String address;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private Boolean onlineOnly;
    private String status;
}

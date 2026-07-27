package kz.ask.business.branch.domain.dto;

import java.math.BigDecimal;
import java.util.List;
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
    private String addressDetails;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String timeZoneId;
    private List<WeeklyOpeningIntervalDto> weeklyHours;
    private List<SpecialOpeningIntervalDto> specialHours;
    private Boolean pickupAvailable;
}

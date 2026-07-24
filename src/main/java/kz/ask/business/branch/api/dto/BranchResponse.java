package kz.ask.business.branch.api.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import kz.ask.business.branch.domain.dto.SpecialOpeningIntervalDto;
import kz.ask.business.branch.domain.dto.WeeklyOpeningIntervalDto;
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
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String timeZoneId;
    private List<WeeklyOpeningIntervalDto> weeklyHours;
    private List<SpecialOpeningIntervalDto> specialHours;
    private BranchOpeningSummaryResponse openingSummary;
}

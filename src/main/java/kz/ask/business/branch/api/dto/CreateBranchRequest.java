package kz.ask.business.branch.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import kz.ask.business.branch.domain.dto.SpecialOpeningIntervalDto;
import kz.ask.business.branch.domain.dto.WeeklyOpeningIntervalDto;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateBranchRequest {
    @NotBlank
    private String name;
    private String address;
    private String addressDetails;
    private UUID cityId;
    @Size(max = 255)
    private String cityName;
    @NotNull
    private BigDecimal latitude;
    @NotNull
    private BigDecimal longitude;
    private String timeZoneId;
    private List<WeeklyOpeningIntervalDto> weeklyHours;
    private List<SpecialOpeningIntervalDto> specialHours;
    private Boolean pickupAvailable;
}

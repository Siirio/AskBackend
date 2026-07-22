package kz.ask.business.branch.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;
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
    private Boolean isOnlineOnly;
    @NotNull
    private BigDecimal latitude;
    @NotNull
    private BigDecimal longitude;
}

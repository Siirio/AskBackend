package kz.ask.business.api.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateBranchRequest {
    @NotBlank
    private String name;
    private String address;
    private UUID cityId;
    private Boolean onlineOnly;
}

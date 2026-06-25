package kz.ask.business.api.dto;

import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateBranchRequest {
    private String name;
    private String address;
    private UUID cityId;
    private Boolean onlineOnly;
}

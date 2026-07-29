package kz.ask.platform.api.dto;

import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PlatformBusinessBranchResponse {

    private UUID branchId;
    private String name;
    private String address;
}

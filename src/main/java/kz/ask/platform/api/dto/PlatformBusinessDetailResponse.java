package kz.ask.platform.api.dto;

import java.util.List;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PlatformBusinessDetailResponse {

    private UUID businessId;
    private String name;
    private String legalName;
    private String bin;
    private String countryCode;
    private String catalogStatus;
    private String businessScope;
    private Long branchCount;
    private Long memberCount;
    private Long productCount;
    private Long serviceCount;
    private Long dropCount;
    private List<PlatformBusinessBranchResponse> branches;
}

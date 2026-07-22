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
    private String moderationStatus;
    private String catalogStatus;
    private String catalogScope;
    private Integer branchCount;
    private Integer memberCount;
    private Long productCount;
    private Long serviceCount;
    private Long dropCount;
    private List<BusinessBranchDto> branches;

    @Getter
    @Builder
    public static class BusinessBranchDto {
        private UUID branchId;
        private String name;
        private String address;
        private Boolean onlineOnly;
    }
}

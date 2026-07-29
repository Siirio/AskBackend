package kz.ask.platform.api.dto;

import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PlatformBusinessRowResponse {

    private UUID businessId;
    private String name;
    private String legalName;
    private String contactEmail;
    private Long branchCount;
    private Long memberCount;
    private Long productCount;
    private Long serviceCount;
    private Long dropCount;
    private String catalogStatus;
}

package kz.ask.platform.api.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PlatformDashboardResponse {

    private Long totalBusinesses;
    private Long totalActiveProducts;
    private Long totalActiveServices;
    private Long totalActiveDrops;
    private Long openSupportConversations;
    private Long pendingModerationItems;
    private Long totalUsers;
}

package kz.ask.search.api.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import kz.ask.contact.api.dto.ContactActionSummaryResponse;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SearchV2CardResponse {
    private String component;
    private UUID resultId;
    private UUID businessId;
    private String businessName;
    private String brandColor;
    private String brandLogoUrl;
    private String title;
    private BigDecimal price;
    private String availability;
    private List<String> badges;
    private Integer distanceMeters;
    private String branchName;
    private Boolean hasActiveDrop;
    private List<ContactActionSummaryResponse> contactActions;
}

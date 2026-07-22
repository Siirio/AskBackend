package kz.ask.search.basic.api.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SearchCardResponse {
    private String component;
    private UUID resultId;
    private UUID businessId;
    private String businessName;
    private String brandColor;
    private String brandLogoUrl;
    private String title;
    private BigDecimal price;
    private String availability;
    private String availabilityWarning;
    private List<String> matchReasons;
    private List<String> badges;
    private Integer distanceMeters;
    private String branchName;
    private String branchAddress;
    private String branchCity;
    private OffsetDateTime workingHourStart;
    private OffsetDateTime workingHourEnd;
}

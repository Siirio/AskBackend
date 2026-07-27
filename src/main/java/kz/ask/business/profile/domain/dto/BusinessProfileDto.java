package kz.ask.business.profile.domain.dto;

import java.util.List;
import java.util.UUID;
import kz.ask.business.profile.domain.enums.DeliveryCoverage;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BusinessProfileDto {
    private UUID id;
    private UUID businessId;
    private String businessName;
    private String brandColor;
    private String logoUrl;
    private String coverUrl;
    private String description;
    private String number;
    private String email;
    private String instagramUrl;
    private String telegramUrl;
    private String websiteUrl;
    private DeliveryCoverage deliveryCoverage;
    private List<String> deliveryCities;
    private Boolean pickupAvailable;
}

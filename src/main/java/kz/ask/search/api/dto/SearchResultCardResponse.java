package kz.ask.search.api.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class SearchResultCardResponse {

    private UUID id;
    private String type;
    private String name;
    private BigDecimal price;
    private UUID businessId;
    private String supplierName;
    private UUID brandId;
    private String businessName;
    private String brandColor;
    private String brandLogoUrl;
    private String brandCoverUrl;
    private String brandDescriptor;
    private String branchAddress;
    private String branchName;
    private String branchContext;
    private String categoryName;
    private String priceText;
    private String availabilityStatus;
    private String confirmationStatus;
    private List<String> pickupOptions;
    private String distanceText;
    private Integer distanceMeters;
    private String source;
    private String sourceType;
    private String publicNote;
    private String confidenceCode;
    private String sectionType;
    private Integer score;
    private List<String> matchReasons;
    private List<String> badges;
    private List<String> warnings;
    private Boolean requiresSupplierCheck;
    private Boolean hasActiveDrop;
    private List<String> contactActions;
    private List<String> availableActions;
}

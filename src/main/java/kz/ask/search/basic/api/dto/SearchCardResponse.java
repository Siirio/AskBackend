package kz.ask.search.basic.api.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import kz.ask.offer.media.CatalogImageResponse;
import kz.ask.offer.purchase.api.dto.PurchaseDestinationResponse;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SearchCardResponse {
    private String component;
    private UUID resultId;
    private UUID businessId;
    private String businessName;
    private String resultType;
    private String brandColor;
    private String brandLogoUrl;
    private String title;
    private String summary;
    private List<CatalogImageResponse> images;
    private List<PurchaseDestinationResponse> purchaseDestinations;
    private String categoryLabel;
    private BigDecimal price;
    private String currency;
    private SearchBusinessProfileResponse businessProfile;
    private String availability;
    private String availabilityWarning;
    private List<String> matchReasons;
    private List<String> badges;
    private Integer distanceMeters;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private Boolean hasActiveOffer;
    private String branchName;
    private String branchAddress;
    private String branchCity;
    private String decisionLabel;
    private List<CriterionAssessmentResponse> criterionAssessments;
    private List<String> advantages;
    private List<String> tradeoffs;
    private List<String> unknowns;
    private List<CriterionEvidenceResponse> comparisonFacts;
}

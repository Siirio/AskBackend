package kz.ask.search.basic.domain.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import kz.ask.search.basic.domain.enums.SearchAvailabilitySource;
import kz.ask.search.basic.domain.enums.SearchAvailabilityStatus;
import kz.ask.search.basic.domain.enums.SearchDocumentType;
import kz.ask.search.basic.domain.enums.SearchProjectionAction;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(toBuilder = true)
public class SearchDocumentDto {

    private SearchDocumentType documentType;
    private UUID aggregateId;
    private UUID businessId;
    private UUID branchId;
    private String title;
    private String normalizedTitle;
    private String summary;
    private String categoryLabel;
    private String businessName;
    private String branchName;
    private String branchAddress;
    private BigDecimal price;
    private String currency;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String city;
    private String country;
    private List<String> tokens;
    private String embeddingText;
    private Map<String, Object> verifiedAttributes;
    private String source;
    private SearchAvailabilityStatus availabilityStatus;
    private SearchAvailabilitySource availabilitySource;
    private Long projectionVersion;
    private Long indexedVersion;
    private SearchProjectionAction projectionAction;
}

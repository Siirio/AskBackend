package kz.ask.search.basic.domain.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import kz.ask.search.basic.domain.enums.SearchAvailabilitySource;
import kz.ask.search.basic.domain.enums.SearchAvailabilityStatus;
import kz.ask.search.basic.domain.enums.SearchDocumentType;
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
    private BigDecimal price;
    private String currency;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private List<String> tokens;
    private String aliases;
    private Map<String, Object> verifiedAttributes;
    private Map<String, Object> aiAttributes;
    private String source;
    private SearchAvailabilityStatus availabilityStatus;
    private SearchAvailabilitySource availabilitySource;
}

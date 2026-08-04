package kz.ask.search.basic.api.dto;

import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SearchCompanyFacetResponse {
    private UUID businessId;
    private String businessName;
    private Integer resultCount;
}

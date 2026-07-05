package kz.ask.search.api.dto;

import java.util.List;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SearchV2Response {
    private UUID searchSessionId;
    private String rawQuery;
    private String scope;
    private String understoodQuery;
    private List<SearchV2SectionResponse> sections;
    private Integer supplierCheckCount;
}

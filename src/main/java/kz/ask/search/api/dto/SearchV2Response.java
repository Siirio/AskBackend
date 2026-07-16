package kz.ask.search.api.dto;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SearchV2Response {
    private String rawQuery;
    private String scope;
    private String understoodQuery;
    private List<SearchV2SectionResponse> sections;
    private List<SearchConstraintResponse> interpretedConstraints;
    private Integer page;
    private Integer pageSize;
    private Integer total;
    private Boolean hasNext;
    private SearchDiagnosticsResponse diagnostics;
}

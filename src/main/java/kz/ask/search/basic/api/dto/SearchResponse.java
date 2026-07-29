package kz.ask.search.basic.api.dto;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SearchResponse {
    private String rawQuery;
    private String mode;
    private String understoodQuery;
    private List<SearchSectionResponse> sections;
    private List<SearchConstraintResponse> interpretedConstraints;
    private Integer page;
    private Integer pageSize;
    private Integer total;
    private Boolean hasNext;
    private String ambiguity;
    private List<String> suggestions;
}

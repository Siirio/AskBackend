package kz.ask.search.basic.api.dto;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SearchSectionResponse {
    private String type;
    private String title;
    private String kind;
    private List<String> relaxedConstraints;
    private String reason;
    private List<SearchCardResponse> cards;
}

package kz.ask.search.api.dto;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SearchV2SectionResponse {
    private String type;
    private String title;
    private List<SearchV2CardResponse> cards;
}

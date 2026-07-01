package kz.ask.search.api.dto;

import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class SearchResultSectionResponse {

    private String type;
    private String title;
    private List<SearchResultCardResponse> items;
}

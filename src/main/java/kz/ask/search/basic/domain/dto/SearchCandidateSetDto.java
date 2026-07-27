package kz.ask.search.basic.domain.dto;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SearchCandidateSetDto {

    private List<SearchCandidateDto> candidates;
    private Boolean semanticLaneAvailable;
}

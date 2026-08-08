package kz.ask.search.decision.api.dto;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CompareResponse {

    private String mode;
    private List<CompareItemResponse> items;
    private List<CompareGroupResponse> groups;
}

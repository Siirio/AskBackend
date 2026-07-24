package kz.ask.business.branch.api.dto;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BranchListResponse {
    private List<BranchResponse> branches;
}

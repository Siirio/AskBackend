package kz.ask.business.api.dto;

import kz.ask.business.domain.enums.BranchMemberRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateStaffRequest {

    private BranchMemberRole role;
    private String status;
}

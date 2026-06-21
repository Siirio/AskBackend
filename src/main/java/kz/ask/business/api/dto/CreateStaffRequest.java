package kz.ask.business.api.dto;

import jakarta.validation.constraints.NotBlank;
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
public class CreateStaffRequest {

    @NotBlank
    private String email;

    @NotBlank
    private String displayName;

    private BranchMemberRole role;
}

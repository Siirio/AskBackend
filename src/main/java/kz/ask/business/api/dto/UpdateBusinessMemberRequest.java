package kz.ask.business.api.dto;

import jakarta.validation.constraints.NotNull;
import kz.ask.business.domain.enums.BusinessMemberRole;
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
public class UpdateBusinessMemberRequest {

    @NotNull
    private BusinessMemberRole role;
}

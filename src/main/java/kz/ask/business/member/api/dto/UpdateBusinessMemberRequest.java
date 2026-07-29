package kz.ask.business.member.api.dto;

import jakarta.validation.constraints.NotNull;
import kz.ask.identity.authorization.domain.enums.Role;
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
    private Role role;
}

package kz.ask.platform.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.Set;
import kz.ask.identity.authorization.domain.enums.Permission;
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
public class CreatePlatformUserRequest {

    @NotBlank
    @Email
    private String email;

    @NotNull
    private Role role;

    @NotEmpty
    private Set<Permission> permissions;
}

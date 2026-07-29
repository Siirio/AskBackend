package kz.ask.platform.domain.dto;

import java.util.Set;
import java.util.UUID;
import kz.ask.identity.authorization.domain.enums.Permission;
import kz.ask.identity.authorization.domain.enums.Role;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class PlatformMembershipDto {

    private UUID id;
    private UUID userId;
    private String email;
    private String displayName;
    private Role role;
    private String status;
    private Set<Permission> permissions;
}

package kz.ask.platform.domain.dto;

import java.util.Set;
import java.util.UUID;
import kz.ask.platform.domain.enums.PlatformPermission;
import kz.ask.platform.domain.enums.PlatformRole;

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
    private PlatformRole role;
    private String status;
    private Set<PlatformPermission> permissions;
}

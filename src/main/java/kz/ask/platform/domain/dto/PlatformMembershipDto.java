package kz.ask.platform.domain.dto;

import java.util.Set;
import java.util.UUID;
import kz.ask.platform.domain.enums.PlatformPermission;
import kz.ask.platform.domain.enums.PlatformRole;
import kz.ask.shared.domain.enums.RecordStatus;
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
    private RecordStatus status;
    private Set<PlatformPermission> permissions;
}

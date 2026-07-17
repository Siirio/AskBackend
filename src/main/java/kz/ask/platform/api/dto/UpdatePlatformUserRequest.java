package kz.ask.platform.api.dto;

import java.util.Set;
import kz.ask.platform.domain.enums.PlatformPermission;
import kz.ask.platform.domain.enums.PlatformRole;
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
public class UpdatePlatformUserRequest {

    private PlatformRole role;
    private Set<PlatformPermission> permissions;
}

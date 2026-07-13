package kz.ask.identity.api.dto;

import java.util.UUID;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class RoleOption {

    private UUID userId;
    private String role;
    private String displayName;
}

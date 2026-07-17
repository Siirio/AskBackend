package kz.ask.identity.api.dto;

import java.util.Set;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class AuthPlatformMembershipResponse {

    private String role;
    private Set<String> permissions;
}

package kz.ask.identity.api.dto;

import java.util.UUID;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class AuthUserResponse {

    private UUID userId;
    private String displayName;
    private String email;
    private String status;
}

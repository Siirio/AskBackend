package kz.ask.identity.domain.dto;

import java.time.Instant;
import java.util.UUID;
import kz.ask.identity.domain.enums.AppRole;
import kz.ask.identity.domain.enums.UserStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class AppUserDto {

    private UUID id;
    private String email;
    private String phone;
    private String displayName;
    private String passwordHash;
    private AppRole role;
    private UserStatus status;
    private Boolean mustChangePassword;
    private String tempPasswordEncrypted;
    private Instant activatedAt;
}

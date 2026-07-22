package kz.ask.identity.domain.dto;

import java.time.Instant;
import java.util.UUID;
import kz.ask.identity.authorization.domain.enums.Role;
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
    private String displayName;
    private String passwordHash;
    private Role role;
    private UserStatus status;
    private Boolean isPasswordChangeRequired;
    private Boolean isTwoFactorEnabled;
    private String tempPasswordEncrypted;
    private Instant activatedAt;
    private Instant lastLoginAt;
}

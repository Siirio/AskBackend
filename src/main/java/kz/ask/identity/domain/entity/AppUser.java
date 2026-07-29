package kz.ask.identity.domain.entity;

import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Convert;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.Instant;
import kz.ask.identity.authorization.domain.enums.Role;
import kz.ask.identity.authorization.infrastructure.persistence.AppUserRoleConverter;
import kz.ask.identity.domain.enums.UserStatus;
import kz.ask.shared.domain.entity.BaseUuidV7Entity;

@Entity
@Getter
@Setter
@Table(name = "app_user")
public class AppUser extends BaseUuidV7Entity {

    private String email;

    @Column(length = 32)
    private String phone;

    @Column(nullable = false)
    private String displayName;

    @Column(nullable = false)
    private String passwordHash;

    @Column(nullable = false)
    @Convert(converter = AppUserRoleConverter.class)
    private Role role;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private UserStatus status;

    @Column(nullable = false)
    private Boolean isPasswordChangeRequired;

    @Column(nullable = false)
    private Boolean isTwoFactorEnabled;

    private String tempPasswordEncrypted;

    private Instant activatedAt;

    private Instant lastLoginAt;
}

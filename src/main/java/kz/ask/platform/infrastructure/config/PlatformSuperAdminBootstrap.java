package kz.ask.platform.infrastructure.config;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;
import kz.ask.identity.domain.IdentityService;
import kz.ask.identity.domain.dto.AppUserDto;
import kz.ask.identity.authorization.domain.enums.Role;
import kz.ask.platform.domain.PlatformMembershipService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PlatformSuperAdminBootstrap implements ApplicationRunner {

    private final PlatformMembershipService platformMembershipService;
    private final IdentityService identityService;
    private final SuperAdminProperties superAdminProperties;

    public PlatformSuperAdminBootstrap(
            PlatformMembershipService platformMembershipService,
            IdentityService identityService,
            SuperAdminProperties superAdminProperties) {
        this.platformMembershipService = platformMembershipService;
        this.identityService = identityService;
        this.superAdminProperties = superAdminProperties;
    }

    @Override
    public void run(ApplicationArguments args) {
        bootstrapConfiguredSuperadmins();
    }

    private void bootstrapConfiguredSuperadmins() {
        List<SuperAdminProperties.SuperAdmin> superadmins = superAdminProperties.getSuperadmins();
        if (superadmins == null || superadmins.isEmpty()) {
            return;
        }
        for (SuperAdminProperties.SuperAdmin sa : superadmins) {
            if (sa.getEmail() == null || sa.getEmail().isBlank()) {
                continue;
            }
            try {
                bootstrapSuperadmin(sa);
            } catch (Exception e) {
                log.error("Failed to bootstrap superadmin {}", sa.getEmail(), e);
            }
        }
    }

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private void bootstrapSuperadmin(SuperAdminProperties.SuperAdmin sa) {
        String password = resolvePassword(sa);
        List<AppUserDto> users = identityService.findAllActiveByEmail(sa.getEmail());
        AppUserDto user;
        if (users.isEmpty()) {
            user = identityService.createUser(
                    sa.getEmail(), sa.getDisplayName(), password, Role.SUPER_ADMIN);
            identityService.activateUser(user.getId());
            log.info("Created PLATFORM_SUPER_ADMIN user: {}", sa.getEmail());
        } else {
            user = users.get(0);
            identityService.changePassword(user.getId(), password);
            log.info("Updated PLATFORM_SUPER_ADMIN password for existing user: {}", sa.getEmail());
        }

        if (platformMembershipService.findActiveByUser(user.getId()) != null) {
            return;
        }
        platformMembershipService.create(
                user.getId(), Role.SUPER_ADMIN, Role.SUPER_ADMIN.getPermissions());
        log.info("Created SUPER_ADMIN platform membership for {}", sa.getEmail());
    }

    private String resolvePassword(SuperAdminProperties.SuperAdmin sa) {
        if (sa.getPassword() != null && !sa.getPassword().isBlank()) {
            return sa.getPassword();
        }
        byte[] extra = new byte[24];
        SECURE_RANDOM.nextBytes(extra);
        String generated = UUID.randomUUID() + "-" + Base64.getUrlEncoder().withoutPadding().encodeToString(extra);
        log.warn("=== SUPERADMIN PASSWORD GENERATED for {}: {} ===", sa.getEmail(), generated);
        return generated;
    }

    @Component
    @ConfigurationProperties(prefix = "ask.platform")
    static class SuperAdminProperties {
        private List<SuperAdmin> superadmins;

        public List<SuperAdmin> getSuperadmins() {
            return superadmins;
        }

        public void setSuperadmins(List<SuperAdmin> superadmins) {
            this.superadmins = superadmins;
        }

        static class SuperAdmin {
            private String email;
            private String password;
            private String displayName;

            public String getEmail() {
                return email;
            }

            public void setEmail(String email) {
                this.email = email;
            }

            public String getPassword() {
                return password;
            }

            public void setPassword(String password) {
                this.password = password;
            }

            public String getDisplayName() {
                return displayName;
            }

            public void setDisplayName(String displayName) {
                this.displayName = displayName;
            }
        }
    }
}

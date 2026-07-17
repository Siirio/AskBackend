package kz.ask.platform.infrastructure.config;

import java.util.EnumSet;
import java.util.List;
import kz.ask.identity.domain.IdentityService;
import kz.ask.identity.domain.dto.AppUserDto;
import kz.ask.platform.domain.PlatformMembershipService;
import kz.ask.platform.domain.enums.PlatformPermission;
import kz.ask.platform.domain.enums.PlatformRole;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PlatformSuperAdminBootstrap implements ApplicationRunner {

    private final PlatformMembershipService platformMembershipService;
    private final IdentityService identityService;
    private final String superAdminEmail;

    public PlatformSuperAdminBootstrap(
            PlatformMembershipService platformMembershipService,
            IdentityService identityService,
            @Value("${ask.platform.super-admin-email:}") String superAdminEmail) {
        this.platformMembershipService = platformMembershipService;
        this.identityService = identityService;
        this.superAdminEmail = superAdminEmail;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (superAdminEmail == null || superAdminEmail.isBlank()) {
            return;
        }
        List<AppUserDto> users = identityService.findAllActiveByEmail(superAdminEmail);
        if (users.isEmpty()) {
            log.warn("Platform super admin bootstrap skipped: no active user for configured email");
            return;
        }
        AppUserDto user = users.get(0);
        if (platformMembershipService.findActiveByUser(user.getId()) != null) {
            return;
        }
        platformMembershipService.create(
                user.getId(), PlatformRole.SUPER_ADMIN, EnumSet.allOf(PlatformPermission.class));
        log.info("Platform super admin membership created for configured email");
    }
}

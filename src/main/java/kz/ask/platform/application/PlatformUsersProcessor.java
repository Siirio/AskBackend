package kz.ask.platform.application;

import java.util.List;
import java.util.UUID;
import kz.ask.identity.domain.IdentityService;
import kz.ask.identity.domain.dto.AppUserDto;
import kz.ask.identity.infrastructure.security.AskPrincipal;
import kz.ask.platform.api.dto.CreatePlatformUserRequest;
import kz.ask.platform.api.dto.UpdatePlatformUserRequest;
import kz.ask.platform.domain.PlatformMembershipService;
import kz.ask.platform.domain.dto.PlatformMembershipDto;
import kz.ask.platform.domain.enums.PlatformPermission;
import kz.ask.shared.error.ErrorCode;
import kz.ask.shared.error.ForbiddenException;
import kz.ask.shared.error.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class PlatformUsersProcessor {

    private final PlatformMembershipService platformMembershipService;
    private final IdentityService identityService;

    @Transactional(readOnly = true)
    public List<PlatformMembershipDto> list(AskPrincipal principal) {
        requireManagePlatformUsers(principal);
        return platformMembershipService.listAll();
    }

    @Transactional
    public PlatformMembershipDto create(AskPrincipal principal, CreatePlatformUserRequest request) {
        requireManagePlatformUsers(principal);
        List<AppUserDto> users = identityService.findAllActiveByEmail(request.getEmail());
        if (users.isEmpty()) {
            throw new NotFoundException(ErrorCode.USER_NOT_FOUND);
        }
        return platformMembershipService.create(
                users.get(0).getId(), request.getRole(), request.getPermissions());
    }

    @Transactional
    public PlatformMembershipDto update(AskPrincipal principal, UUID membershipId, UpdatePlatformUserRequest request) {
        requireManagePlatformUsers(principal);
        return platformMembershipService.update(membershipId, request.getRole(), request.getPermissions());
    }

    @Transactional
    public PlatformMembershipDto deactivate(AskPrincipal principal, UUID membershipId) {
        requireManagePlatformUsers(principal);
        return platformMembershipService.deactivate(membershipId);
    }

    private void requireManagePlatformUsers(AskPrincipal principal) {
        PlatformMembershipDto membership = platformMembershipService.findActiveByUser(principal.getUserId());
        if (membership == null
                || !membership.getPermissions().contains(PlatformPermission.MANAGE_PLATFORM_USERS)) {
            throw new ForbiddenException(ErrorCode.ACCESS_DENIED);
        }
    }
}

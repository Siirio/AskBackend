package kz.ask.platform.application;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import kz.ask.audit.domain.SignificantEventService;
import kz.ask.audit.domain.enums.SignificantEventType;
import kz.ask.identity.domain.IdentityService;
import kz.ask.identity.domain.dto.AppUserDto;
import kz.ask.identity.authorization.domain.enums.Permission;
import kz.ask.identity.authorization.domain.enums.Role;
import kz.ask.identity.infrastructure.security.AskPrincipal;
import kz.ask.platform.api.dto.CreateAdminRequest;
import kz.ask.platform.api.dto.CreatePlatformUserRequest;
import kz.ask.platform.api.dto.UpdatePlatformUserRequest;
import kz.ask.platform.domain.PlatformMembershipService;
import kz.ask.platform.domain.dto.PlatformMembershipDto;
import kz.ask.shared.error.ErrorCode;
import kz.ask.shared.error.ForbiddenException;
import kz.ask.shared.error.NotFoundException;
import kz.ask.shared.error.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class PlatformUsersProcessor {

    private final PlatformMembershipService platformMembershipService;
    private final IdentityService identityService;
    private final SignificantEventService significantEventService;

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

    @Transactional
    public PlatformMembershipDto createAdmin(AskPrincipal principal, CreateAdminRequest request) {
        requireSuperAdmin(principal);

        if (identityService.emailExists(request.getEmail())) {
            throw new ValidationException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        Role appRole = request.getRole();
        AppUserDto user = identityService.createUser(
                request.getEmail(), request.getDisplayName(), request.getPassword(), appRole);
        identityService.activateUser(user.getId());

        PlatformMembershipDto membership = platformMembershipService.create(
                user.getId(), request.getRole(), request.getRole().getPermissions());

        significantEventService.record(
                principal.getUserId(), SignificantEventType.PLATFORM_ADMIN_CREATED,
                null, membership.getId(),
                Map.of("adminUserId", user.getId(), "adminEmail", request.getEmail(),
                        "adminRole", request.getRole().name()));

        return membership;
    }

    @Transactional
    public void deleteAdmin(AskPrincipal principal, UUID membershipId) {
        requireSuperAdmin(principal);

        PlatformMembershipDto target = platformMembershipService.listAll().stream()
                .filter(m -> m.getId().equals(membershipId))
                .findFirst()
                .orElseThrow(() -> new NotFoundException(ErrorCode.PLATFORM_MEMBERSHIP_NOT_FOUND));

        if (target.getUserId().equals(principal.getUserId())) {
            throw new ValidationException(ErrorCode.CANNOT_DELETE_SELF);
        }

        if (target.getRole() == Role.SUPER_ADMIN
                && platformMembershipService.countByRole(Role.SUPER_ADMIN) <= 1) {
            throw new ValidationException(ErrorCode.CANNOT_DELETE_LAST_SUPER_ADMIN);
        }

        UUID adminUserId = target.getUserId();
        String adminEmail = target.getEmail();
        String adminRole = target.getRole().name();

        platformMembershipService.delete(membershipId);
        identityService.updateUserStatus(adminUserId, "DELETED");

        significantEventService.record(
                principal.getUserId(), SignificantEventType.PLATFORM_ADMIN_DELETED,
                null, membershipId,
                Map.of("adminUserId", adminUserId, "adminEmail", adminEmail,
                        "adminRole", adminRole));
    }

    private void requireManagePlatformUsers(AskPrincipal principal) {
        PlatformMembershipDto membership = platformMembershipService.findActiveByUser(principal.getUserId());
        if (membership == null
                || !membership.getPermissions().contains(Permission.MANAGE_PLATFORM_USERS)) {
            throw new ForbiddenException(ErrorCode.ACCESS_DENIED);
        }
    }

    private void requireSuperAdmin(AskPrincipal principal) {
        PlatformMembershipDto membership = platformMembershipService.findActiveByUser(principal.getUserId());
        if (membership == null || membership.getRole() != Role.SUPER_ADMIN) {
            throw new ForbiddenException(ErrorCode.ACCESS_DENIED);
        }
    }

}

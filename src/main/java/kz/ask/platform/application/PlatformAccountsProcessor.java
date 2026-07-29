package kz.ask.platform.application;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.UUID;
import kz.ask.business.member.domain.entity.BusinessMember;
import kz.ask.business.member.infrastructure.repository.BusinessMemberRepository;
import kz.ask.identity.application.AccountLifecycleProcessor;
import kz.ask.identity.authorization.domain.enums.Permission;
import kz.ask.identity.domain.entity.AppUser;
import kz.ask.identity.domain.enums.UserStatus;
import kz.ask.identity.infrastructure.repository.AppUserRepository;
import kz.ask.identity.infrastructure.security.AskPrincipal;
import kz.ask.platform.api.dto.PlatformAccountListResponse;
import kz.ask.platform.api.dto.PlatformAccountResponse;
import kz.ask.platform.domain.PlatformMembershipService;
import kz.ask.platform.domain.dto.PlatformMembershipDto;
import kz.ask.shared.error.ErrorCode;
import kz.ask.shared.error.ConflictException;
import kz.ask.shared.error.ForbiddenException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class PlatformAccountsProcessor {

    private static final int MAX_PAGE_SIZE = 100;

    private final AppUserRepository appUserRepository;
    private final BusinessMemberRepository businessMemberRepository;
    private final PlatformMembershipService platformMembershipService;
    private final AccountLifecycleProcessor accountLifecycleProcessor;

    @Transactional(readOnly = true)
    public PlatformAccountListResponse list(
            AskPrincipal principal,
            int page,
            int size,
            String query,
            UserStatus status) {
        requireAnyPermission(principal, Permission.MODERATE_APP_USERS, Permission.MANAGE_PLATFORM_USERS);
        Page<AppUser> users = appUserRepository.searchForPlatform(
                normalizeQuery(query),
                status,
                PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), MAX_PAGE_SIZE)));
        return PlatformAccountListResponse.builder()
                .items(users.getContent().stream().map(this::toResponse).toList())
                .page(users.getNumber())
                .size(users.getSize())
                .totalElements(users.getTotalElements())
                .totalPages(users.getTotalPages())
                .build();
    }

    @Transactional
    public void delete(AskPrincipal principal, UUID userId, String reason) {
        requireAnyPermission(principal, Permission.MANAGE_PLATFORM_USERS);
        if (principal.getUserId().equals(userId)) {
            throw new ConflictException(ErrorCode.CANNOT_DELETE_SELF);
        }
        accountLifecycleProcessor.deleteByPlatform(principal.getUserId(), userId, reason);
    }

    private PlatformAccountResponse toResponse(AppUser user) {
        List<String> businessNames = new LinkedHashSet<>(
                businessMemberRepository.findByUserId(user.getId()).stream()
                        .map(BusinessMember::getBusiness)
                        .map(business -> business.getName())
                        .toList())
                .stream()
                .toList();
        return PlatformAccountResponse.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .displayName(user.getDisplayName())
                .status(user.getStatus().name())
                .businessNames(businessNames)
                .createdAt(user.getCreatedAt())
                .build();
    }

    private String normalizeQuery(String query) {
        if (query == null || query.isBlank()) {
            return "";
        }
        return "%" + query.trim().toLowerCase() + "%";
    }

    private void requireAnyPermission(AskPrincipal principal, Permission... permissions) {
        PlatformMembershipDto membership = platformMembershipService.findActiveByUser(principal.getUserId());
        if (membership == null) {
            throw new ForbiddenException(ErrorCode.ACCESS_DENIED);
        }
        for (Permission permission : permissions) {
            if (membership.getPermissions().contains(permission)) {
                return;
            }
        }
        throw new ForbiddenException(ErrorCode.ACCESS_DENIED);
    }
}

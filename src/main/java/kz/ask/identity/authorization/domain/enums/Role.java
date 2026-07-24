package kz.ask.identity.authorization.domain.enums;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

public enum Role {
    SUPER_ADMIN(RoleGroup.PLATFORM, allPlatformPermissions()),
    ADMIN(RoleGroup.PLATFORM, allPlatformPermissions()),
    MODERATOR(RoleGroup.PLATFORM, moderatorPermissions()),
    OWNER(RoleGroup.BUSINESS, EnumSet.noneOf(Permission.class)),
    MANAGER(RoleGroup.BUSINESS, EnumSet.noneOf(Permission.class)),
    WORKER(RoleGroup.BUSINESS, EnumSet.noneOf(Permission.class)),
    CUSTOMER(RoleGroup.CUSTOMER, EnumSet.noneOf(Permission.class));

    private final RoleGroup group;
    private final Set<Permission> permissions;

    Role(RoleGroup group, Set<Permission> permissions) {
        this.group = group;
        this.permissions = Collections.unmodifiableSet(
                permissions.isEmpty() ? EnumSet.noneOf(Permission.class) : EnumSet.copyOf(permissions));
    }

    public RoleGroup getGroup() {
        return group;
    }

    public Set<Permission> getPermissions() {
        return permissions;
    }

    private static EnumSet<Permission> allPlatformPermissions() {
        EnumSet<Permission> permissions = EnumSet.allOf(Permission.class);
        permissions.remove(Permission.EDIT_ITEMS_SERVICES_DURING_IMPORT);
        return permissions;
    }

    private static EnumSet<Permission> moderatorPermissions() {
        return EnumSet.of(
                Permission.MODERATE_ITEMS,
                Permission.MODERATE_SERVICES,
                Permission.MODERATE_UNIQUE_OFFERS,
                Permission.MODERATE_BUSINESSES,
                Permission.MODERATE_BRANCHES,
                Permission.MODERATE_BUSINESS_MEMBERS,
                Permission.MODERATE_APP_USERS,
                Permission.MODERATE_CHATS,
                Permission.VIEW_MODERATION_QUEUE,
                Permission.MANAGE_SUPPORT_CHATS
        );
    }
}

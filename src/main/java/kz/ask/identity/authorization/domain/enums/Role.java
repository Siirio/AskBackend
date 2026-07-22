package kz.ask.identity.authorization.domain.enums;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

public enum Role {
    SUPER_ADMIN(RoleGroup.PLATFORM, EnumSet.allOf(Permission.class)),
    ADMIN(RoleGroup.PLATFORM, EnumSet.allOf(Permission.class)),
    MODERATOR(RoleGroup.PLATFORM, EnumSet.allOf(Permission.class)),
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
}

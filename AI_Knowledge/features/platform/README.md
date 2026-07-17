# Platform — Why This Exists

Platform staff (ASK employees) operate managed imports, support chats, moderation, and platform user administration. Spec §14 (platform cabinet), §11 (catalog capabilities), §16 (significant events).

## Model
- PlatformMembership: user ↔ PlatformRole (SUPER_ADMIN/ADMIN/MODERATOR) + Set<PlatformPermission>, RecordStatus
- Role = base profile; permissions = fine-tuning. Checks always test permissions, never role.
- PlatformPermission: MANAGE_PLATFORM_USERS, MANAGE_MANAGED_IMPORTS, EDIT_CATALOG_DURING_IMPORT, PUBLISH_CATALOG_DURING_IMPORT, MANAGE_SUPPORT_CHATS, MODERATE_CONTENT, SUSPEND_BUSINESS, BAN_BUSINESS

## Key Decisions
- Super-admin bootstrap via `ask.platform.super-admin-email` (ApplicationRunner) — solves the chicken-and-egg: creating platform users requires MANAGE_PLATFORM_USERS. Grants SUPER_ADMIN + all permissions to the configured existing user if no active membership exists.
- Platform catalog access is indirect: EDIT_CATALOG_DURING_IMPORT alone is not enough — an ACTIVE managed-import grant for the target business is also required (CatalogCapabilityService.hasPlatformCatalogAccess).
- Platform chat is restricted to MANAGED_IMPORT conversations; GENERAL_SUPPORT is private to customer↔business.

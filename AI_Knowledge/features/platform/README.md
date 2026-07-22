# Platform — Why This Exists

Platform staff (ASK employees) operate supported imports, support chats, moderation, and platform user administration. They do not own Business, Item, or Service data; they act through explicit permissions.

## Model
- PlatformMembership: user ↔ PlatformRole (SUPER_ADMIN/ADMIN/MODERATOR) + Set<PlatformPermission>, RecordStatus
- Role = base profile; permissions = fine-tuning. Checks always test permissions, never role.
- PlatformPermission: MANAGE_PLATFORM_USERS, MANAGE_MANAGED_IMPORTS, EDIT_ITEMS_SERVICES_DURING_IMPORT, PUBLISH_ITEMS_SERVICES_DURING_IMPORT, MANAGE_SUPPORT_CHATS, MODERATE_CONTENT

## Key Decisions
- Moderation is recorded in the generic `ModerationAction` target relation (`targetType`, `targetId`, `moderationStatus`). It does not add a moderation-status field to `Business`.
- Super-admin bootstrap via `ask.platform.super-admin-email` (ApplicationRunner) — solves the chicken-and-egg: creating platform users requires MANAGE_PLATFORM_USERS. Grants SUPER_ADMIN + all permissions to the configured existing user if no active membership exists.
- Platform Item/Service access is indirect: EDIT_ITEMS_SERVICES_DURING_IMPORT alone is not enough — an ACTIVE managed-import grant for the target business is also required. Item actions require ITEM or BOTH scope; service actions require SERVICE or BOTH scope.
- Platform chat is restricted to MANAGED_IMPORT conversations; GENERAL_SUPPORT is private to customer↔business.

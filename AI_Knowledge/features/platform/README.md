# Platform — Why This Exists

Platform staff (ASK employees) operate supported imports, support chats, moderation, and platform user administration. They do not own Business, Item, or Service data; they act through explicit permissions.

## Model
- PlatformMembership: user ↔ PlatformRole (SUPER_ADMIN/ADMIN/MODERATOR) + Set<PlatformPermission>, RecordStatus
- Role = base profile; permissions = fine-tuning. Checks always test permissions, never role.
- PlatformPermission: MANAGE_PLATFORM_USERS, MANAGE_MANAGED_IMPORTS, USE_AI_ITEMS_SERVICES_TOOLS, PUBLISH_ITEMS_SERVICES_DURING_IMPORT, MANAGE_SUPPORT_CHATS, MODERATE_CONTENT

## Key Decisions
- Moderation is recorded in the generic `ModerationAction` target relation (`targetType`, `targetId`, `moderationStatus`). It does not add a moderation-status field to `Business`.
- Super-admin bootstrap via `ask.platform.super-admin-email` (ApplicationRunner) solves the chicken-and-egg: creating platform users requires MANAGE_PLATFORM_USERS. The default platform permission set excludes catalog editing because that entitlement is business-specific.
- Activating a managed-import request immediately assigns its platform member the requested `ITEM`, `SERVICE`, or `BOTH` catalog access for that Business until `expiresAt`, configured as seven days. No global catalog-edit permission is required.
- Platform members with `MANAGE_SUPPORT_CHATS` may inspect and operate customer↔business and platform-support conversations. Managed-import conversations additionally require `MANAGE_MANAGED_IMPORTS` and the assigned active grant.

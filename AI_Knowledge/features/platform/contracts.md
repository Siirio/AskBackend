# Platform — REST API Contracts

## Platform Users (requires MANAGE_PLATFORM_USERS)
| Method | Path | Purpose |
|--------|------|---------|
| GET | /api/v1/platform/users | List all platform memberships |
| POST | /api/v1/platform/users | Create membership for existing user by email → 201 |
| PATCH | /api/v1/platform/users/{membershipId} | Update role and/or permissions (null-safe partial) |
| POST | /api/v1/platform/users/{membershipId}/deactivate | Set status INACTIVE |

## Platform Moderation (requires granular permissions)
| Method | Path | Purpose |
|--------|------|---------|
| POST | /api/v1/platform/moderation-actions | Execute moderation action (BLOCK/UNBLOCK/FLAG/APPROVE/REJECT) |
| GET | /api/v1/platform/moderation/queue | View moderation queue (requires VIEW_MODERATION_QUEUE) |
| GET | /api/v1/platform/reports | List open reports (requires VIEW_MODERATION_QUEUE) |
| PATCH | /api/v1/platform/reports/{reportId} | Resolve report (requires VIEW_MODERATION_QUEUE) |

## Moderation Permissions (replaces monolithic MODERATE_CONTENT)
- MODERATE_ITEMS, MODERATE_SERVICES, MODERATE_UNIQUE_OFFERS, MODERATE_BUSINESSES
- MODERATE_BRANCHES, MODERATE_BUSINESS_MEMBERS, MODERATE_APP_USERS, MODERATE_CHATS
- VIEW_MODERATION_QUEUE

## ModerationActionRequest
```json
{
  "targetType": "PRODUCT|SERVICE|BUSINESS|USER|MESSAGE",
  "targetId": "uuid",
  "action": "BLOCK|UNBLOCK|FLAG|APPROVE|REJECT",
  "reasonCode": "string?",
  "note": "string?",
  "expiresAt": "ISO8601?"
}
```

## Key DTOs
- PlatformMembershipDto: id, userId, email, displayName, role, status, permissions
- CreatePlatformUserRequest: email (@Email), role, permissions (@NotEmpty)
- UpdatePlatformUserRequest: role?, permissions? (replace-all when present)

## Errors
- 403 ACCESS_DENIED — caller lacks required permission
- 404 USER_NOT_FOUND — no active user with that email
- 400 PLATFORM_MEMBERSHIP_EXISTS — user already has an ACTIVE membership

## Config
- ask.platform.super-admin-email / ASK_PLATFORM_SUPER_ADMIN_EMAIL — bootstrap SUPER_ADMIN on startup

## Role Permissions
- SUPER_ADMIN, ADMIN: all permissions except EDIT_ITEMS_SERVICES_DURING_IMPORT
- MODERATOR: moderation permissions + MANAGE_SUPPORT_CHATS (not MANAGE_PLATFORM_USERS)

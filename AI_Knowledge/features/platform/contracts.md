# Platform — REST API Contracts

## Platform Users (requires MANAGE_PLATFORM_USERS)
| Method | Path | Purpose |
|--------|------|---------|
| GET | /api/v1/platform/users | List all platform memberships |
| POST | /api/v1/platform/users | Create membership for existing user by email → 201 |
| PATCH | /api/v1/platform/users/{membershipId} | Update role and/or permissions (null-safe partial) |
| POST | /api/v1/platform/users/{membershipId}/deactivate | Set status INACTIVE |

## Platform Moderation (requires granular permissions)
The queue contains only suspicious Item, Service, Message, Business, or AppUser events created by automated checks or explicit reports. Normal Item and Service creation is immediately searchable and does not create a queue row.
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
  "targetType": "PRODUCT|SERVICE|UNIQUE_OFFER|BUSINESS|USER|MESSAGE",
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
- ask.platform.super-admin-email / ASK_PLATFORM_SUPER_ADMIN_EMAIL — create a missing SUPER_ADMIN identity and membership on startup without changing an existing identity's password

## Role Permissions
- SUPER_ADMIN, ADMIN: all permissions except EDIT_ITEMS_SERVICES_DURING_IMPORT
- MODERATOR: moderation permissions + MANAGE_SUPPORT_CHATS (not MANAGE_PLATFORM_USERS)

## Platform Cabinet

Primary sections are `SUMMARY`, `BUSINESSES`, `CHATS`, `ACCOUNTS`, and `TEAM`. Chat filters use canonical conversation types `PLATFORM_SUPPORT`, `MANAGED_IMPORT`, and `GENERAL_SUPPORT`.

Navigation event counters count unresolved moderation events by backend severity `REVIEW` or `CRITICAL`; they do not count ordinary unread messages.

## Cabinet Data Endpoints

- `GET /api/v1/platform/dashboard` returns summary totals.
- `GET /api/v1/platform/businesses` returns a searchable paginated business list.
- `GET /api/v1/platform/businesses/{businessId}` returns business facts and branches.
- Nested `/products` and `/services` endpoints expose the complete business catalog to platform staff without requiring business membership.
- `GET /api/v1/platform/catalog/items`, `/services`, and `/drops` return platform-wide paginated catalog views without opening a Business workspace.
- `GET /api/v1/platform/accounts` returns a searchable paginated account list.
- `DELETE /api/v1/platform/accounts/{userId}` runs the account anonymization flow and requires platform-user management permission.
- Moderation actions change the target state: Item and Service visibility is synchronized with search, account blocks update `UserStatus`, and business blocks remove their catalog projections from search.
- Platform catalog moderation supports block, restore, and superadmin soft-delete for Item, Service, and UniqueOffer targets.
- Automated detection analyzes names, descriptions, categories, tags, schedules, and attributes. High-confidence illegal signals are hidden and emitted as `CRITICAL`; regulated or ambiguous signals are hidden pending `REVIEW`. Clear content still publishes immediately.
- Detection uses normalized Unicode and word-aware patterns so short ambiguous words do not match as arbitrary substrings.

# Platform — REST API Contracts

## Platform Users (requires MANAGE_PLATFORM_USERS)
| Method | Path | Purpose |
|--------|------|---------|
| GET | /api/v1/platform/users | List all platform memberships |
| POST | /api/v1/platform/users | Create membership for existing user by email → 201 |
| PATCH | /api/v1/platform/users/{membershipId} | Update role and/or permissions (null-safe partial) |
| POST | /api/v1/platform/users/{membershipId}/deactivate | Set status INACTIVE |

## Key DTOs
- PlatformMembershipDto: id, userId, email, displayName, role, status, permissions
- CreatePlatformUserRequest: email (@Email), role, permissions (@NotEmpty)
- UpdatePlatformUserRequest: role?, permissions? (replace-all when present)

## Errors
- 403 ACCESS_DENIED — caller lacks MANAGE_PLATFORM_USERS
- 404 USER_NOT_FOUND — no active user with that email
- 400 PLATFORM_MEMBERSHIP_EXISTS — user already has an ACTIVE membership

## Config
- ask.platform.super-admin-email / ASK_PLATFORM_SUPER_ADMIN_EMAIL — bootstrap SUPER_ADMIN on startup (skip if blank, user missing, or membership exists)

## Related Surfaces (other features)
- Platform chat: see features/messaging/contracts.md
- Catalog capabilities for platform members: see features/catalog/contracts.md
- Managed imports: /api/v1/platform/managed-imports (PlatformManagedImportController), /api/v1/businesses/{businessId}/managed-imports (BusinessManagedImportController)
- Resolving a content report requires a `RESOLVED` or `REJECTED` status plus a non-blank resolution; only an `OPEN` report can transition.
- `GET/PATCH /api/v1/platform/catalog-reviews` provides moderator decisions for partial seven-day catalogs.
- `POST /api/v1/platform/ai-enrichment` queues selected documents and requires `USE_AI_CATALOG_TOOLS`.

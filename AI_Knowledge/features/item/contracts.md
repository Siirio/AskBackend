# Item — REST API Contracts

Public customers discover Items through the unified search endpoint; this feature does not expose a
separate public listing or detail API in the current backend contract.

## Business item endpoints

| Method | Path | Auth | Purpose |
| --- | --- | --- | --- |
| GET | /api/v1/businesses/{businessId}/items?branchId={optional} | OWNER/STAFF | List business items, optionally narrowed to a branch |
| POST | /api/v1/businesses/{businessId}/items | OWNER/STAFF | Create an Item with an optional `branchId` |
| PATCH | /api/v1/items/{itemId} | OWNER/STAFF | Update an Item (derives businessId from entity) |
| DELETE | /api/v1/items/{itemId} | OWNER/STAFF | Hard-delete an Item (derives businessId from entity) |

## Item data

- An Item belongs to a Business.
- It stores one `ITEM` category identity, description, deepLink, tags, attributes, price, isActive, and moderationStatus.
- A branch association is optional and contains only location-specific facts; creating an Item never creates a branch.
- Item PATCH preserves the current branch when `branchId` is omitted or null. The current contract has no branch-clear operation; a non-null branch is verified against the owning Business.
- The client supplies a selected category or explicitly requests creation of a `USER` category. Free-form category labels are not canonical data.
- `isActive` is business-controlled (owner/staff toggle via PATCH /items/{itemId}).
- `moderationStatus` is platform-controlled (PENDING/APPROVED/REJECTED).
- Search visibility requires BOTH `isActive == true` AND `moderationStatus == APPROVED`.
- Public search returns a compact Item row plus the complete public Business profile needed by the detail modal.

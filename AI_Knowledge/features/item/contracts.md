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
- It stores one `ITEM` category identity, description, labeled purchase destinations, tags, attributes, price, isActive, and moderationStatus.
- Target purchase-destination contract: an ordered list of `{ label, url }` entries owned by the Item. The current singular `deepLink` field must be replaced or migrated before this target contract is exposed; it must not be multiplied by branch.
- Purchase destinations are public links deliberately supplied for customers. Business-verification or moderation sources are never eligible purchase destinations.
- A branch association is optional and contains only location-specific facts; creating an Item never creates a branch.
- Item PATCH preserves the current branch when `branchId` is omitted or null. The current contract has no branch-clear operation; a non-null branch is verified against the owning Business.
- The client supplies a selected category or explicitly requests creation of a `USER` category. Free-form category labels are not canonical data.
- `isActive` is business-controlled (owner/staff toggle via PATCH /items/{itemId}).
- Create synchronously assigns `moderationStatus`: `APPROVED` for normal Items and `REJECTED` when the prohibited-keyword autoban matches. Item creation has no pending manual-approval gate.
- Search visibility requires BOTH `isActive == true` AND `moderationStatus == APPROVED`; normal Items therefore publish immediately, while autobanned Items remain saved in the business cabinet but stay out of search.
- Business list responses are ordered by `createdAt DESC`.
- Public search returns a compact Item row plus the complete public Business profile needed by the detail modal.
- Public search must expose the Item purchase destinations before the customer-facing `Proceed to Purchase` action is rendered.

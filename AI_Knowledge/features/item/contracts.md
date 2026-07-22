# Item — REST API Contracts

Public customers discover Items through the unified search endpoint; this feature does not expose a
separate public listing or detail API in the current backend contract.

## Business item endpoints

| Method | Path | Auth | Purpose |
| --- | --- | --- | --- |
| GET | /api/v1/businesses/{businessId}/items?branchId={optional} | OWNER/MANAGER/WORKER | List business items, optionally narrowed to a branch |
| POST | /api/v1/businesses/{businessId}/items | OWNER/MANAGER/WORKER | Create an Item with an optional `branchId` |
| PATCH | /api/v1/businesses/{businessId}/items/{itemId} | OWNER/MANAGER/WORKER | Update an Item |
| DELETE | /api/v1/businesses/{businessId}/items/{itemId} | OWNER/MANAGER/WORKER | Disable an Item |

## Item data

- An Item belongs to a Business.
- It stores one `ITEM` category identity, description, tags, attributes, and status.
- A branch association is optional and contains only location-specific facts; creating an Item never creates a branch.
- The client supplies a selected category or explicitly requests creation of a `USER` category. Free-form category labels are not canonical data.

# Service — REST API Contracts

Public customers discover Services through the unified search endpoint; this feature does not expose
a separate public listing or detail API in the current backend contract.

## Business service endpoints

| Method | Path | Auth | Purpose |
| --- | --- | --- | --- |
| GET | /api/v1/businesses/{businessId}/services?branchId={optional} | OWNER/MANAGER/WORKER | List business services, optionally narrowed to a branch |
| POST | /api/v1/businesses/{businessId}/services | OWNER/MANAGER/WORKER | Create a Service with an optional `branchId` |
| PATCH | /api/v1/businesses/{businessId}/services/{serviceId} | OWNER/MANAGER/WORKER | Update a Service |
| DELETE | /api/v1/businesses/{businessId}/services/{serviceId} | OWNER/MANAGER/WORKER | Delete a Service and publish durable search deletion |

## Service data

- A Service belongs to a Business.
- It stores one `SERVICE` category identity, description, and canonical attributes.
- A branch association is optional and contains only location-specific facts such as price, schedule, and visibility.
- Active Services are searchable immediately after creation; there is no moderation approval gate.
- Business list responses are ordered by `createdAt DESC`.
- Service PATCH preserves the current branch when `branchId` is omitted or null. The current contract has no branch-clear operation; a non-null branch is verified against the owning Business.
- A service card can explicitly open the shared customer-to-business conversation. Search creates neither bookings nor requests.
- Public search returns a compact Service row plus the complete public Business profile needed by the detail modal.

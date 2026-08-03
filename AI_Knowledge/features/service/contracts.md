# Service — REST API Contracts

## Managed images

`POST /api/v1/businesses/{businessId}/services/{serviceOfferingId}/images` consumes multipart form data. Repeated `files` parts contain new PNG, JPEG, or WebP files. Repeated `order` values define the final gallery using retained stored identifiers or `new:{zeroBasedFileIndex}` tokens. The final gallery contains at most three images and the first image is primary. Responses expose ordered `images` entries with server-generated `id` and `url`; clients never submit external media URLs.

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
- It stores one `SERVICE` category identity, description, labeled purchase destinations, and canonical attributes.
- `purchaseDestinations` is the current ordered list of `{ label, url }` entries owned by the Service; URLs must use HTTP(S). It is accepted by create/update and returned by business rows and public search. Business-verification or moderation sources are not eligible.
- Purchase destinations are never attached to a branch. A branch remains optional location, price, schedule, and visibility context.
- A branch association is optional and contains only location-specific facts such as price, schedule, and visibility.
- Active Services are searchable immediately after creation; there is no moderation approval gate.
- Business list responses are ordered by `createdAt DESC`.
- Service PATCH preserves the current branch when `branchId` is omitted or null. The current contract has no branch-clear operation; a non-null branch is verified against the owning Business.
- A service card can explicitly open the shared customer-to-business conversation. Search creates neither bookings nor requests.
- Public search returns a compact Service row plus the complete public Business profile needed by the detail modal.
- Public search must expose the Service purchase destinations before the customer-facing `Proceed to Purchase` action is rendered.

# Service — REST API Contracts

## Client Service Endpoints
| Method | Path | Auth | Purpose |
|--------|------|------|---------|
| GET | /api/v1/services | No | Search/list services |
| GET | /api/v1/services/{offerId} | No | Service detail |

## Business Admin Service Endpoints
| Method | Path | Auth | Purpose |
|--------|------|------|---------|
| GET | /api/v1/business-admin/branches/{branchId}/services | OWNER/MANAGER/WORKER | List branch services |
| POST | /api/v1/business-admin/branches/{branchId}/services | OWNER/MANAGER/WORKER | Create service + offer |
| PATCH | /api/v1/business-admin/branches/{branchId}/services/{serviceOfferingId} | OWNER/MANAGER/WORKER | Update service |

## Service Branch Offer Model
- service_offering: business_id, category_id, name, description, attributes (JSONB), image_url, status
- service_branch_offer: service_offering_id, branch_id, base_price, schedule_text, active, status
- ServiceBranchOffer is the only branch relationship; duplicating to another branch creates a new offer with copied initial price.

## Customer contact

A service card may explicitly open or resume the shared customer-to-business conversation. Search does not create supplier responses, bookings, or calendar reservations.

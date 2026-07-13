# Service — REST API Contracts

## Client Service Endpoints
| Method | Path | Auth | Purpose |
|--------|------|------|---------|
| GET | /api/v1/services | No | Search/list services |
| GET | /api/v1/services/{offerId} | No | Service detail |

## Business Admin Service Endpoints
| Method | Path | Auth | Purpose |
|--------|------|------|---------|
| GET | /api/v1/business-admin/branches/{branchId}/services | OWNER/MANAGER/STAFF | List branch services |
| POST | /api/v1/business-admin/branches/{branchId}/services | OWNER/MANAGER/STAFF | Create service + offer |
| PATCH | /api/v1/business-admin/branches/{branchId}/services/{offerId} | OWNER/MANAGER/STAFF | Update service |
| DELETE | /api/v1/business-admin/branches/{branchId}/services/{offerId} | OWNER/MANAGER/STAFF | Delete/deactivate service |

## Service Branch Offer Model
- service_offering: business_id, category_id, name, description, status
- service_branch_offer: service_offering_id, branch_id, base_price, duration_minutes, schedule_text, active, status
- service_branch M2M: one service can appear in multiple branches

## Supplier Response Statuses (Service)
- CAN_PROVIDE (+ confirmedStartAt/EndAt) → creates booking, CONFIRMED
- CANNOT_PROVIDE → CONFIRMATION_DECLINED
- NEED_CLARIFICATION → DISCUSSING
- SUGGEST_OTHER_TIME (+ proposedStartAt) → DISCUSSING

## ActivityDisplayStatus (derived, NOT stored)
- DISCUSSING: default (CAN_PROVIDE without time, SUGGEST_OTHER_TIME, NEED_CLARIFICATION)
- CONFIRMED: CAN_PROVIDE + confirmedStartAt != null
- CONFIRMATION_DECLINED: CANNOT_PROVIDE

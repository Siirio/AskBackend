# Retired Requests — REST API Contracts

The routes below are legacy contracts scheduled for removal. The approved item/service search flow does not call them and does not create supplier checks.

## Customer Requests
| Method | Path | Auth | Purpose |
|--------|------|------|---------|
| POST | /api/v1/requests | CUSTOMER | Create fallback request |
| GET | /api/v1/requests | CUSTOMER | List my requests |
| GET | /api/v1/requests/{requestId} | CUSTOMER | Request detail with responses |

## Business Responses
| Method | Path | Auth | Purpose |
|--------|------|------|---------|
| GET | /api/v1/business-admin/requests | BUSINESS | List received requests (Activity) |
| POST | /api/v1/business-admin/requests/{targetId}/respond | BUSINESS | Send supplier response |

## Request Status Lifecycle
DRAFT → CREATED → DISPATCHING → SENT → PARTIALLY_RESPONDED → COMPLETED
+ EXPIRED, CANCELLED, FAILED

## Supplier Response Statuses
Product: HAS_ITEM, NO_ITEM, NEED_CLARIFICATION, HAS_ANALOG
Service: CAN_PROVIDE, CANNOT_PROVIDE, NEED_CLARIFICATION, SUGGEST_OTHER_TIME

## Response Source Types
AUTO_REPLY, STAFF_REPLY, BUSINESS_CONFIRMED, DATA_UPDATED, SUPPLIER_CHECK_CONFIRMED
(AUTO_REPLY does NOT count as confirmation)

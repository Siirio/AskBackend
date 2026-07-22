# Import — REST API Contracts

## Excel Import
Base: /api/v1/business-admin/branches/{branchId}/item-imports

| Method | Path | Auth | Purpose |
|--------|------|------|---------|
| POST | /item-imports | OWNER/WORKER | Upload .xlsx → parse → auto-map → MAPPING_REQUIRED |
| POST | /item-imports/{importId}/mapping | OWNER/WORKER | Save mapping → normalize → PREVIEW_READY |
| GET | /item-imports/{importId}/preview | OWNER/WORKER | Get preview state |
| POST | /item-imports/{importId}/approve | OWNER/WORKER | Create Product + Offer + SearchDocument → IMPORTED |
| POST | /item-imports/{importId}/cancel | OWNER/WORKER | Cancel → CANCELLED |

## AI Autodump Import
| Method | Path | Auth | Purpose |
|--------|------|------|---------|
| POST | /api/v1/business-admin/branches/{branchId}/autodump-sessions/files | Assigned platform importer | Upload .txt/.md/.pdf for AI item or service processing |

## Managed Import
- `POST /api/v1/businesses/{businessId}/managed-imports` creates a PENDING request without a grant or chat. Request requires `catalog_scope` = PRODUCTS, SERVICES, or BOTH, plus source types, source details, contact channel/value, and legal acceptance.
- `POST /api/v1/platform/managed-imports/{requestId}/activate` assigns the request, creates the seven-day grant for the request's catalog scope, and starts the managed-import chat.
- `GET /api/v1/platform/managed-imports/businesses/{businessId}/catalog-access` returns `allowed` and the active `catalogScope`; the assigned importer can edit products and/or services inside that scope.
- There is no manual completion endpoint. Expiry records item count, revokes the grant, and deletes chat attachments.

## Import Statuses
CatalogImportStatus: UPLOADED → MAPPING_REQUIRED → PREVIEW_READY → IMPORTED / FAILED / CANCELLED
RawRowStatus: PENDING, VALID, WARNING, INVALID
TargetField: NAME, CATEGORY_LABEL, DESCRIPTION, SKU, PRICE, TAGS, IGNORE, APPEND_TO_DESCRIPTION, CHARACTERISTIC
